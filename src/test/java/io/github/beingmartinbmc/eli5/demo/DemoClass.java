package io.github.beingmartinbmc.eli5.demo;

import io.github.beingmartinbmc.eli5.annotations.ExplainLikeImFive;

/**
 * Demo class showing how to use the @ExplainLikeImFive annotation.
 * This class demonstrates various use cases for the annotation.
 */
@ExplainLikeImFive(prompt = "This is a utility class for mathematical operations")
public class DemoClass {
    
    @ExplainLikeImFive
    public static final double PI = 3.14159265359;
    
    @ExplainLikeImFive(prompt = "This method calculates the factorial of a number using recursion")
    public int factorial(final int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }
    
    @ExplainLikeImFive(prompt = "This method calculates the greatest common divisor using Euclidean algorithm")
    public int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    @ExplainLikeImFive(includeBody = false, prompt = "This method checks if a number is prime")
    public boolean isPrime(int number) {
        if (number <= 1) return false;
        if (number <= 3) return true;
        if (number % 2 == 0 || number % 3 == 0) return false;
        
        for (int i = 5; i * i <= number; i += 6) {
            if (number % i == 0 || number % (i + 2) == 0) {
                return false;
            }
        }
        return true;
    }
    
    @ExplainLikeImFive(prompt = "This method calculates the Fibonacci sequence using dynamic programming")
    public long fibonacci(int n) {
        if (n <= 1) return n;
        
        long[] fib = new long[n + 1];
        fib[0] = 0;
        fib[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            fib[i] = fib[i - 1] + fib[i - 2];
        }
        
        return fib[n];
    }
    
    // Method without annotation - won't be processed
    public void regularMethod() {
        System.out.println("This method doesn't have the @ExplainLikeImFive annotation");
    }
}
