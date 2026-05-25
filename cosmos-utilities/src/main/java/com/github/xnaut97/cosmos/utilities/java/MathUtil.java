package com.github.xnaut97.cosmos.utilities.java;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class MathUtil {

	/*
	 * =========================
	 * Percentage
	 * =========================
	 */

	/**
	 * Returns progress percentage between start and end.
	 *
	 * Example:
	 * start = 0
	 * end = 100
	 * value = 50
	 *
	 * result = 50
	 */
	public BigDecimal findPercentInRange(double start,
	                                     double end,
	                                     double value) {

		double range = end - start;

		if (range == 0D) {
			return BigDecimal.ZERO;
		}

		double percent =
				((value - start) / range) * 100D;

		return BigDecimal.valueOf(percent);
	}

	/**
	 * Converts percentage into value inside range.
	 *
	 * Example:
	 * start = 0
	 * end = 200
	 * percent = 50
	 *
	 * result = 100
	 */
	public BigDecimal fromPercentToNumber(double start,
	                                      double end,
	                                      double percent) {

		double value =
				start + ((percent / 100D) * (end - start));

		return BigDecimal.valueOf(value);
	}

	/**
	 * Returns percentage of number1 from number2.
	 */
	public BigDecimal getPercent(double number1,
	                             double number2) {

		return getPercent(
				number1,
				number2,
				RoundingMode.HALF_UP,
				1
		);
	}

	public BigDecimal getPercent(double number1,
	                             double number2,
	                             RoundingMode roundingMode,
	                             int scale) {

		if (number2 == 0D) {
			return BigDecimal.ZERO;
		}

		double percent =
				(number1 / number2) * 100D;

		return BigDecimal.valueOf(percent)
				.setScale(scale, roundingMode);
	}

	/*
	 * =========================
	 * Rounding
	 * =========================
	 */

	public double round(double value) {

		return round(value, 2);
	}

	public double round(double value,
	                    int scale) {

		return BigDecimal.valueOf(value)
				.setScale(scale, RoundingMode.HALF_UP)
				.doubleValue();
	}

	/**
	 * Always rounds upward.
	 */
	public int ceil(double value) {

		return BigDecimal.valueOf(value)
				.setScale(0, RoundingMode.CEILING)
				.intValue();
	}

	/**
	 * Always rounds downward.
	 */
	public int floor(double value) {

		return BigDecimal.valueOf(value)
				.setScale(0, RoundingMode.FLOOR)
				.intValue();
	}

	/*
	 * =========================
	 * Clamp
	 * =========================
	 */

	public double clamp(double value,
	                    double min,
	                    double max) {

		return Math.max(min, Math.min(max, value));
	}

	public int clamp(int value,
	                 int min,
	                 int max) {

		return Math.max(min, Math.min(max, value));
	}

	/*
	 * =========================
	 * Expression Evaluator
	 * =========================
	 */

	public int eval(String expression) {

		if (expression == null
				|| expression.trim().isEmpty()) {

			return 0;
		}

		return (int) new ExpressionParser(expression)
				.parse();
	}

	/*
	 * =========================
	 * Internal Parser
	 * =========================
	 */

	private static class ExpressionParser {

		private final String expression;

		private int pos = -1;

		private int ch;

		private ExpressionParser(String expression) {
			this.expression = expression;
		}

		private void nextChar() {

			ch = (++pos < expression.length())
					? expression.charAt(pos)
					: -1;
		}

		private boolean eat(int charToEat) {

			while (ch == ' ') {
				nextChar();
			}

			if (ch == charToEat) {
				nextChar();
				return true;
			}

			return false;
		}

		private double parse() {

			nextChar();

			double x = parseExpression();

			if (pos < expression.length()) {
				throw new RuntimeException(
						"Unexpected: " + (char) ch
				);
			}

			return x;
		}

		private double parseExpression() {

			double x = parseTerm();

			while (true) {

				if (eat('+')) {
					x += parseTerm();
				} else if (eat('-')) {
					x -= parseTerm();
				} else {
					return x;
				}
			}
		}

		private double parseTerm() {

			double x = parseFactor();

			while (true) {

				if (eat('*')) {
					x *= parseFactor();
				} else if (eat('/')) {
					x /= parseFactor();
				} else {
					return x;
				}
			}
		}

		private double parseFactor() {

			if (eat('+')) {
				return parseFactor();
			}

			if (eat('-')) {
				return -parseFactor();
			}

			double x;

			int startPos = pos;

			if (eat('(')) {

				x = parseExpression();

				eat(')');

			} else if ((ch >= '0' && ch <= '9')
					|| ch == '.') {

				while ((ch >= '0' && ch <= '9')
						|| ch == '.') {

					nextChar();
				}

				x = Double.parseDouble(
						expression.substring(startPos, pos)
				);

			} else if (ch >= 'a' && ch <= 'z') {

				while (ch >= 'a' && ch <= 'z') {
					nextChar();
				}

				String function =
						expression.substring(startPos, pos);

				x = parseFactor();

				switch (function) {

					case "sqrt":
						x = Math.sqrt(x);
						break;

					case "sin":
						x = Math.sin(Math.toRadians(x));
						break;

					case "cos":
						x = Math.cos(Math.toRadians(x));
						break;

					case "tan":
						x = Math.tan(Math.toRadians(x));
						break;

					default:
						throw new RuntimeException(
								"Unknown function: " + function
						);
				}

			} else {

				throw new RuntimeException(
						"Unexpected: " + (char) ch
				);
			}

			if (eat('^')) {
				x = Math.pow(x, parseFactor());
			}

			return x;
		}

	}

}