package com.dashboard.budget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class sandbox {

	public static void main(String[] args) {

		System.out.println("duplicates: " + hasDuplicateChars("anaconda"));
		System.out.println("is anagram: " + isAnagram("aa"));

		int[] unsorted = { 3, 54, 1, 56 };
		System.out.println("merge-sort: unsorted: " + Arrays.toString(unsorted));
		int[] sorted = mergeSort(unsorted, 0, unsorted.length - 1);
		System.out.println("merge-sort: sorted: " + Arrays.toString(sorted));

		System.out.println("sum as recursion: " + recSum(51, 14));
		System.out.println("first n squares: " + firstSquares(5));

	}

	private static int firstSquares(int n) {
		if (n == 0)
			return 0;
		else
			return n * n + firstSquares(n - 1);
	}

	private static int[] mergeSort(int[] arr, int start, int end) {
		if (start == end) {
			return new int[] { arr[start] };
		} else {
			int middle = start + (end - start) / 2;
			int[] left = mergeSort(arr, start, middle);
			int[] right = mergeSort(arr, middle + 1, end);
			return merge(left, right);
		}
	}

	private static int[] merge(int[] left, int[] right) {
		int[] result = new int[left.length + right.length];
		int i = 0;
		int j = 0;
		while (i < left.length && j < right.length) {
			if (left[i] < right[j]) {
				result[i + j] = left[i];
				i++;
			} else {
				result[i + j] = right[j];
				j++;
			}
		}
		while (i < left.length) {
			result[i + j] = left[i];
			i++;
		}
		while (j < right.length) {
			result[i + j] = right[j];
			j++;
		}

		return result;
	}

	private static boolean isAnagram(String input) {
		int length = input.length();
		if (length < 2)
			return false;

		int middle = length / 2;

		for (int i = 0; i < middle; i++) {
			if (!input.substring(i, i + 1).equals(input.substring(length - i - 1, length - i)))
				return false;
		}

		return true;
	}

	private static int recSum(int a, int b) {
		if (b == 0)
			return a;
		return 1 + recSum(a, b - 1);
	}

	private static boolean hasDuplicateChars(String input) {
		if (input.length() < 2)
			return false;

		List<String> symbols = new ArrayList<String>();

		for (int i = 0; i < input.length(); i++)
			if (symbols.contains(input.substring(i, i + 1)))
				return true;
			else
				symbols.add(input.substring(i, i + 1));

		return false;
	}
}
