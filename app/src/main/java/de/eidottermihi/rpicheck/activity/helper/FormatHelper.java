/**
 * MIT License
 *
 * Copyright (c) 2018  RasPi Check Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package de.eidottermihi.rpicheck.activity.helper;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Utility class for conversion, formatting, etc.
 *
 * @author Michael
 */
public class FormatHelper {
    public static final String SCALE_CELSIUS = "°C";
    public static final String SCALE_FAHRENHEIT = "°F";

    public static final String SCALE_HZ = "Hz";
    public static final String SCALE_MHZ = "MHz";
    public static final String SCALE_GHZ = "GHz";

    private static final String N_A = " n/a ";

    private static final NumberFormat decimalFormat = DecimalFormat
            .getNumberInstance();

    /**
     * Formats a temperature in celsius.
     *
     * @param tempInCelsius the temperature to be formatted (in celsius)
     * @param tempScale     the scale to use (°C/°F).
     * @return the formatted temperature
     */
    public static String formatTemperature(double tempInCelsius,
                                           String tempScale) {
        if (tempScale.equals(SCALE_CELSIUS)) {
            return decimalFormat.format(tempInCelsius) + SCALE_CELSIUS;
        } else {
            return decimalFormat.format(celsiusToFahrenheit(tempInCelsius))
                    + SCALE_FAHRENHEIT;
        }
    }

    public static double celsiusToFahrenheit(double celsius) {
        return celsius * 1.8 + 32;
    }

    /**
     * Formats a frequency in Hz.
     *
     * @param frequencyInHz the frequency in Hz
     * @param scale         the scale (Hz/MHz/GHz)
     * @return the formatted frequency
     */
    public static String formatFrequency(long frequencyInHz, String scale) {
        if (scale.equals(SCALE_HZ)) {
            return decimalFormat.format(frequencyInHz) + " " + scale;
        } else if (scale.equals(SCALE_GHZ)) {
            final String output = decimalFormat.format(new BigDecimal(
                    frequencyInHz).divide(new BigDecimal(1000000000))
                    .doubleValue())
                    + " " + scale;
            return output;
        } else {
            return decimalFormat.format(frequencyInHz / 1000000) + " " + scale;
        }
    }

    /**
     * Formats a Number.
     *
     * @param number            the number
     * @param maxFractionDigits max fraction digits
     * @return the formatted number
     */
    public static String formatDecimal(double number) {
        return decimalFormat.format(number);
    }

    /**
     * Formats a percentage value (0-100).
     *
     * @param percentage the percentage value (0-100).
     * @return formatted string "[percentage] %".
     */
    public static String formatPercentage(Integer percentage) {
        if (percentage != null) {
            return percentage + " %";
        } else {
            return N_A;
        }
    }

    /**
     * Formats the wifi-signal. If the number is negative, it is possibly a dBm
     * value.
     *
     * @param signal the signal (may be null)
     * @return the formatted signal
     */
    public static String formatWifiSignale(Integer signal) {
        if (signal != null) {
            if (signal > 0) {
                // percent value
                return formatPercentage(signal);
            } else {
                // dBm value
                return formatDbmValue(signal);
            }
        } else {
            return N_A;
        }
    }

    /**
     * Formats a dBm value.
     *
     * @param signal the wifi signal in dBm
     * @return the formatted dBm signal
     */
    public static String formatDbmValue(Integer signal) {
        if (signal != null) {
            return signal + " dBm";
        } else {
            return N_A;
        }
    }

}
