import { useState, useCallback } from "react";

interface RetryOptions {
  maxAttempts?: number;
  delay?: number;
  backoffMultiplier?: number;
}

interface RetryState {
  attempts: number;
  isRetrying: boolean;
  lastError: Error | null;
}

export function useRetry(options: RetryOptions = {}) {
  const {
    maxAttempts = 3,
    delay = 1000,
    backoffMultiplier = 2,
  } = options;

  const [state, setState] = useState<RetryState>({
    attempts: 0,
    isRetrying: false,
    lastError: null,
  });

  const executeWithRetry = useCallback(
    async <T>(operation: () => Promise<T>): Promise<T> => {
      setState(prev => ({ ...prev, isRetrying: true, lastError: null }));

      for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        try {
          const result = await operation();
          setState({
            attempts: attempt,
            isRetrying: false,
            lastError: null,
          });
          return result;
        } catch (error) {
          const isLastAttempt = attempt === maxAttempts;
          
          setState(prev => ({
            ...prev,
            attempts: attempt,
            lastError: error as Error,
            isRetrying: !isLastAttempt,
          }));

          if (isLastAttempt) {
            throw error;
          }

          // Wait before retrying with exponential backoff
          const waitTime = delay * Math.pow(backoffMultiplier, attempt - 1);
          await new Promise(resolve => setTimeout(resolve, waitTime));
        }
      }

      throw new Error("Max retry attempts reached");
    },
    [maxAttempts, delay, backoffMultiplier]
  );

  const reset = useCallback(() => {
    setState({
      attempts: 0,
      isRetrying: false,
      lastError: null,
    });
  }, []);

  return {
    ...state,
    executeWithRetry,
    reset,
    canRetry: state.attempts < maxAttempts,
  };
}