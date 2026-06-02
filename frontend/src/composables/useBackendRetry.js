// frontend/src/composables/useBackendRetry.js
import { ref } from 'vue';

export function useBackendRetry(options = {}) {
  const { maxRetries = 3, retryDelay = 10 } = options;
  const retryDelayMs = retryDelay * 1000;

  const isRetrying = ref(false);
  const countdown = ref(0);
  const retryCount = ref(0);
  const errorMessage = ref(null);
  let retryTimeout = null;
  let countdownInterval = null;

  const stopCountdown = () => {
    if (countdownInterval) clearInterval(countdownInterval);
    countdownInterval = null;
  };

  const stopRetry = () => {
    if (retryTimeout) clearTimeout(retryTimeout);
    retryTimeout = null;
    stopCountdown();
    isRetrying.value = false;
    countdown.value = 0;
  };

  const reset = () => {
    stopRetry();
    retryCount.value = 0;
    errorMessage.value = null;
  };

  const startRetry = async (retryFn) => {
    if (retryCount.value >= maxRetries) return;
    if (isRetrying.value) return;

    isRetrying.value = true;
    let secondsLeft = retryDelay;
    countdown.value = secondsLeft;

    stopCountdown();
    countdownInterval = setInterval(() => {
      if (secondsLeft > 0) {
        secondsLeft--;
        countdown.value = secondsLeft;
      } else {
        stopCountdown();
      }
    }, 1000);

    retryTimeout = setTimeout(async () => {
      stopCountdown();
      retryCount.value++;
      isRetrying.value = false;

      const result = await retryFn();

      if (!result.success && retryCount.value < maxRetries) {
        startRetry(retryFn);
      } else if (!result.success) {
        if (result.errorType === 'cold') {
          errorMessage.value = '後端啟動失敗，請稍後再試';
        } else {
          errorMessage.value = '前後端接口異常，請稍後再試';
        }
        isRetrying.value = false;
      } else {
        reset();
      }
    }, retryDelayMs);
  };

  const manualRetry = () => {
    stopRetry();
    retryCount.value = 0;
    errorMessage.value = null;
  };

  const setError = (msg) => {
    errorMessage.value = msg;
    isRetrying.value = false;
    stopRetry();
  };

  return {
    isRetrying,
    countdown,
    retryCount,
    maxRetries,
    errorMessage,
    startRetry,
    manualRetry,
    reset,
    setError,
    stopRetry,
  };
}