/* 
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.FieldEvents.BlurEvent;
import com.vaadin.event.FieldEvents.BlurListener;
import com.vaadin.event.FieldEvents.FocusEvent;
import com.vaadin.event.FieldEvents.FocusListener;
import com.vaadin.shared.ui.datefield.DateFieldConstants;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.Vaadin6Component;

/**
 * <p>
 * A date editor component that can be bound to any {@link Property} that is
 * compatible with <code>java.util.Date</code>.
 * </p>
 * <p>
 * Since <code>DateField</code> extends <code>AbstractField</code> it implements
 * the {@link com.vaadin.data.Buffered}interface.
 * </p>
 * <p>
 * A <code>DateField</code> is in write-through mode by default, so
 * {@link com.vaadin.ui.AbstractField#setWriteThrough(boolean)}must be called to
 * enable buffering.
 * </p>
 * 
 * @author Vaadin Ltd.
 * @since 3.0
 */
@SuppressWarnings("serial")
public class DateField extends AbstractField<Date> implements
        FieldEvents.BlurNotifier, FieldEvents.FocusNotifier, Vaadin6Component {

    /**
     * Resolutions for DateFields
     * 
     * @author Vaadin Ltd.
     * @since 7.0
     */
    public enum Resolution {
        SECOND(Calendar.SECOND), MINUTE(Calendar.MINUTE), HOUR(
                Calendar.HOUR_OF_DAY), DAY(Calendar.DAY_OF_MONTH), MONTH(
                Calendar.MONTH), YEAR(Calendar.YEAR);

        private int calendarField;

        private Resolution(int calendarField) {
            this.calendarField = calendarField;
        }

        /**
         * Returns the field in {@link Calendar} that corresponds to this
         * resolution.
         * 
         * @return one of the field numbers used by Calendar
         */
        public int getCalendarField() {
            return calendarField;
        }

        /**
         * Returns the resolutions that are higher or equal to the given
         * resolution, starting from the given resolution. In other words
         * passing DAY to this methods returns DAY,MONTH,YEAR
         * 
         * @param r
         *            The resolution to start from
         * @return An iterable for the resolutions higher or equal to r
         */
        public static Iterable<Resolution> getResolutionsHigherOrEqualTo(
                Resolution r) {
            List<Resolution> resolutions = new ArrayList<DateField.Resolution>();
            Resolution[] values = Resolution.values();
            for (int i = r.ordinal(); i < values.length; i++) {
                resolutions.add(values[i]);
            }
            return resolutions;
        }

        /**
         * Returns the resolutions that are lower than the given resolution,
         * starting from the given resolution. In other words passing DAY to
         * this methods returns HOUR,MINUTE,SECOND.
         * 
         * @param r
         *            The resolution to start from
         * @return An iterable for the resolutions lower than r
         */
        public static List<Resolution> getResolutionsLowerThan(Resolution r) {
            List<Resolution> resolutions = new ArrayList<DateField.Resolution>();
            Resolution[] values = Resolution.values();
            for (int i = r.ordinal() - 1; i >= 0; i--) {
                resolutions.add(values[i]);
            }
            return resolutions;
        }
    };

    /**
     * Resolution identifier: seconds.
     * 
     * @deprecated As of 7.0, use {@link Resolution#SECOND}
     */
    @Deprecated
    public static final Resolution RESOLUTION_SEC = Resolution.SECOND;

    /**
     * Resolution identifier: minutes.
     * 
     * @deprecated As of 7.0, use {@link Resolution#MINUTE}
     */
    @Deprecated
    public static final Resolution RESOLUTION_MIN = Resolution.MINUTE;

    /**
     * Resolution identifier: hours.
     * 
     * @deprecated As of 7.0, use {@link Resolution#HOUR}
     */
    @Deprecated
    public static final Resolution RESOLUTION_HOUR = Resolution.HOUR;

    /**
     * Resolution identifier: days.
     * 
     * @deprecated As of 7.0, use {@link Resolution#DAY}
     */
    @Deprecated
    public static final Resolution RESOLUTION_DAY = Resolution.DAY;

    /**
     * Resolution identifier: months.
     * 
     * @deprecated As of 7.0, use {@link Resolution#MONTH}
     */
    @Deprecated
    public static final Resolution RESOLUTION_MONTH = Resolution.MONTH;

    /**
     * Resolution identifier: years.
     * 
     * @deprecated As of 7.0, use {@link Resolution#YEAR}
     */
    @Deprecated
    public static final Resolution RESOLUTION_YEAR = Resolution.YEAR;

    /**
     * Specified smallest modifiable unit for the date field.
     */
    private Resolution resolution = Resolution.DAY;

    /**
     * The internal calendar to be used in java.utl.Date conversions.
     */
    private transient Calendar calendar;

    /**
     * Overridden format string
     */
    private String dateFormat;

    private boolean lenient = false;

    private String dateString = null;

    /**
     * Was the last entered string parsable? If this flag is false, datefields
     * internal validator does not pass.
     */
    private boolean uiHasValidDateString = true;

    /**
     * Determines if week numbers are shown in the date selector.
     */
    private boolean showISOWeekNumbers = false;

    private String currentParseErrorMessage;

    private String defaultParseErrorMessage = "Date format not recognized";

    private TimeZone timeZone = null;

    private static Map<Resolution, String> variableNameForResolution = new HashMap<DateField.Resolution, String>();
    {
        variableNameForResolution.put(Resolution.SECOND, "sec");
        variableNameForResolution.put(Resolution.MINUTE, "min");
        variableNameForResolution.put(Resolution.HOUR, "hour");
        variableNameForResolution.put(Resolution.DAY, "day");
        variableNameForResolution.put(Resolution.MONTH, "month");
        variableNameForResolution.put(Resolution.YEAR, "year");
    }

    /* Constructors */

    /**
     * Constructs an empty <code>DateField</code> with no caption.
     */
    public DateField() {
    }

    /**
     * Constructs an empty <code>DateField</code> with caption.
     * 
     * @param caption
     *            the caption of the datefield.
     */
    public DateField(String caption) {
        setCaption(caption);
    }

    /**
     * Constructs a new <code>DateField</code> that's bound to the specified
     * <code>Property</code> and has the given caption <code>String</code>.
     * 
     * @param caption
     *            the caption <code>String</code> for the editor.
     * @param dataSource
     *            the Property to be edited with this editor.
     */
    public DateField(String caption, Property dataSource) {
        this(dataSource);
        setCaption(caption);
    }

    /**
     * Constructs a new <code>DateField</code> that's bound to the specified
     * <code>Property</code> and has no caption.
     * 
     * @param dataSource
     *            the Property to be edited with this editor.
     */
    public DateField(Property dataSource) throws IllegalArgumentException {
        if (!Date.class.isAssignableFrom(dataSource.getType())) {
            throw new IllegalArgumentException("Can't use "
                    + dataSource.getType().getName()
                    + " typed property as datasource");
        }

        setPropertyDataSource(dataSource);
    }

    /**
     * Constructs a new <code>DateField</code> with the given caption and
     * initial text contents. The editor constructed this way will not be bound
     * to a Property unless
     * {@link com.vaadin.data.Property.Viewer#setPropertyDataSource(Property)}
     * is called to bind it.
     * 
     * @param caption
     *            the caption <code>String</code> for the editor.
     * @param value
     *            the Date value.
     */
    public DateField(String caption, Date value) {
        setValue(value);
        setCaption(caption);
    }

    /* Component basic features */

    /*
     * Paints this component. Don't add a JavaDoc comment here, we use the
     * default documentation from implemented interface.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {

        // Adds the locale as attribute
        final Locale l = getLocale();
        if (l != null) {
            target.addAttribute("locale", l.toString());
        }

        if (getDateFormat() != null) {
            target.addAttribute("format", dateFormat);
        }

        if (!isLenient()) {
            target.addAttribute("strict", true);
        }

        target.addAttribute(DateFieldConstants.ATTR_WEEK_NUMBERS,
                isShowISOWeekNumbers());
        target.addAttribute("parsable", uiHasValidDateString);
        /*
         * TODO communicate back the invalid date string? E.g. returning back to
         * app or refresh.
         */

        // Gets the calendar
        final Calendar calendar = getCalendar();
        final Date currentDate = getValue();

        // Only paint variables for the resolution and up, e.g. Resolution DAY
        // paints DAY,MONTH,YEAR
        for (Resolution res : Resolution
                .getResolutionsHigherOrEqualTo(resolution)) {
            int value = -1;
            if (currentDate != null) {
                value = calendar.get(res.getCalendarField());
                if (res == Resolution.MONTH) {
                    // Calendar month is zero based
                    value++;
                }
            }
            target.addVariable(this, variableNameForResolution.get(res), value);
        }
    }

    @Override
    protected boolean shouldHideErrors() {
        return super.shouldHideErrors() && uiHasValidDateString;
    }

    /*
     * Invoked when a variable of the component changes. Don't add a JavaDoc
     * comment here, we use the default documentation from implemented
     * interface.
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {

        if (!isReadOnly()
                && (variables.containsKey("year")
                        || variables.containsKey("month")
                        || variables.containsKey("day")
                        || variables.containsKey("hour")
                        || variables.containsKey("min")
                        || variables.containsKey("sec")
                        || variables.containsKey("msec") || variables
                            .containsKey("dateString"))) {

            // Old and new dates
            final Date oldDate = getValue();
            Date newDate = null;

            // this enables analyzing invalid input on the server
            final String newDateString = (String) variables.get("dateString");
            dateString = newDateString;

            // Gets the new date in parts
            boolean hasChanges = false;
            Map<Resolution, Integer> calendarFieldChanges = new HashMap<DateField.Resolution, Integer>();

            for (Resolution r : Resolution
                    .getResolutionsHigherOrEqualTo(resolution)) {
                // Only handle what the client is allowed to send. The same
                // resolutions that are painted
                String variableName = variableNameForResolution.get(r);

                if (variables.containsKey(variableName)) {
                    Integer value = (Integer) variables.get(variableName);
                    if (r == Resolution.MONTH) {
                        // Calendar MONTH is zero based
                        value--;
                    }
                    if (value >= 0) {
                        hasChanges = true;
                        calendarFieldChanges.put(r, value);
                    }
                }
            }

            // If no new variable values were received, use the previous value
            if (!hasChanges) {
                newDate = null;
            } else {
                // Clone the calendar for date operation
                final Calendar cal = getCalendar();

                // Update the value based on the received info
                // Must set in this order to avoid invalid dates (or wrong
                // dates if lenient is true) in calendar
                for (int r = Resolution.YEAR.ordinal(); r >= 0; r--) {
                    Resolution res = Resolution.values()[r];
                    if (calendarFieldChanges.containsKey(res)) {

                        // Field resolution should be included. Others are
                        // skipped so that client can not make unexpected
                        // changes (e.g. day change even though resolution is
                        // year).
                        Integer newValue = calendarFieldChanges.get(res);
                        cal.set(res.getCalendarField(), newValue);
                    }
                }
                newDate = cal.getTime();
            }

            if (newDate == null && dateString != null && !"".equals(dateString)) {
                try {
                    Date parsedDate = handleUnparsableDateString(dateString);
                    setValue(parsedDate, true);

                    /*
                     * Ensure the value is sent to the client if the value is
                     * set to the same as the previous (#4304). Does not repaint
                     * if handleUnparsableDateString throws an exception. In
                     * this case the invalid text remains in the DateField.
                     */
                    markAsDirty();
                } catch (Converter.ConversionException e) {

                    /*
                     * Datefield now contains some text that could't be parsed
                     * into date.
                     */
                    if (oldDate != null) {
                        /*
                         * Set the logic value to null.
                         */
                        setValue(null);
                        /*
                         * Reset the dateString (overridden to null by setValue)
                         */
                        dateString = newDateString;
                    }

                    /*
                     * Saves the localized message of parse error. This can be
                     * overridden in handleUnparsableDateString. The message
                     * will later be used to show a validation error.
                     */
                    currentParseErrorMessage = e.getLocalizedMessage();

                    /*
                     * The value of the DateField should be null if an invalid
                     * value has been given. Not using setValue() since we do
                     * not want to cause the client side value to change.
                     */
                    uiHasValidDateString = false;

                    /*
                     * Because of our custom implementation of isValid(), that
                     * also checks the parsingSucceeded flag, we must also
                     * notify the form (if this is used in one) that the
                     * validity of this field has changed.
                     * 
                     * Normally fields validity doesn't change without value
                     * change and form depends on this implementation detail.
                     */
                    notifyFormOfValidityChange();
                    markAsDirty();
                }
            } else if (newDate != oldDate
                    && (newDate == null || !newDate.equals(oldDate))) {
                setValue(newDate, true); // Don't require a repaint, client
                // updates itself
            } else if (!uiHasValidDateString) { // oldDate ==
                                                // newDate == null
                // Empty value set, previously contained unparsable date string,
                // clear related internal fields
                setValue(null);
            }
        }

        if (variables.containsKey(FocusEvent.EVENT_ID)) {
            fireEvent(new FocusEvent(this));
        }

        if (variables.containsKey(BlurEvent.EVENT_ID)) {
            fireEvent(new BlurEvent(this));
        }
    }

    /**
     * This method is called to handle a non-empty date string from the client
     * if the client could not parse it as a Date.
     * 
     * By default, a Converter.ConversionException is thrown, and the current
     * value is not modified.
     * 
     * This can be overridden to handle conversions, to return null (equivalent
     * to empty input), to throw an exception or to fire an event.
     * 
     * @param dateString
     * @return parsed Date
     * @throws Converter.ConversionException
     *             to keep the old value and indicate an error
     */
    protected Date handleUnparsableDateString(String dateString)
            throws Converter.ConversionException {
        currentParseErrorMessage = null;
        throw new Converter.ConversionException(getParseErrorMessage());
    }

    /* Property features */

    /*
     * Gets the edited property's type. Don't add a JavaDoc comment here, we use
     * the default documentation from implemented interface.
     */
    @Override
    public Class<Date> getType() {
        return Date.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.vaadin.ui.AbstractField#setValue(java.lang.Object, boolean)
     */
    @Override
    protected void setValue(Date newValue, boolean repaintIsNotNeeded)
            throws Property.ReadOnlyException {

        /*
         * First handle special case when the client side component have a date
         * string but value is null (e.g. unparsable date string typed in by the
         * user). No value changes should happen, but we need to do some
         * internal housekeeping.
         */
        if (newValue == null && !uiHasValidDateString) {
            /*
             * Side-effects of setInternalValue clears possible previous strings
             * and flags about invalid input.
             */
            setInternalValue(null);

            /*
             * Due to DateField's special implementation of isValid(),
             * datefields validity may change although the logical value does
             * not change. This is an issue for Form which expects that validity
             * of Fields cannot change unless actual value changes.
             * 
             * So we check if this field is inside a form and the form has
             * registered this as a field. In this case we repaint the form.
             * Without this hacky solution the form might not be able to clean
             * validation errors etc. We could avoid this by firing an extra
             * value change event, but feels like at least as bad solution as
             * this.
             */
            notifyFormOfValidityChange();
            markAsDirty();
            return;
        }

        super.setValue(newValue, repaintIsNotNeeded);
    }

    /**
     * Detects if this field is used in a Form (logically) and if so, notifies
     * it (by repainting it) that the validity of this field might have changed.
     */
    private void notifyFormOfValidityChange() {
        Component parenOfDateField = getParent();
        boolean formFound = false;
        while (parenOfDateField != null || formFound) {
            if (parenOfDateField instanceof Form) {
                Form f = (Form) parenOfDateField;
                Collection<?> visibleItemProperties = f.getItemPropertyIds();
                for (Object fieldId : visibleItemProperties) {
                    Field<?> field = f.getField(fieldId);
                    if (field == this) {
                        /*
                         * this datefield is logically in a form. Do the same
                         * thing as form does in its value change listener that
                         * it registers to all fields.
                         */
                        f.markAsDirty();
                        formFound = true;
                        break;
                    }
                }
            }
            if (formFound) {
                break;
            }
            parenOfDateField = parenOfDateField.getParent();
        }
    }

    @Override
    protected void setInternalValue(Date newValue) {
        // Also set the internal dateString
        if (newValue != null) {
            dateString = newValue.toString();
        } else {
            dateString = null;
        }

        if (!uiHasValidDateString) {
            // clear component error and parsing flag
            setComponentError(null);
            uiHasValidDateString = true;
            currentParseErrorMessage = null;
        }

        super.setInternalValue(newValue);
    }

    /**
     * Gets the resolution.
     * 
     * @return int
     */
    public Resolution getResolution() {
        return resolution;
    }

    /**
     * Sets the resolution of the DateField.
     * 
     * The default resolution is {@link Resolution#DAY} since Vaadin 7.0.
     * 
     * @param resolution
     *            the resolution to set.
     */
    public void setResolution(Resolution resolution) {
        this.resolution = resolution;
        markAsDirty();
    }

    /**
     * Returns new instance calendar used in Date conversions.
     * 
     * Returns new clone of the calendar object initialized using the the
     * current date (if available)
     * 
     * If this is no calendar is assigned the <code>Calendar.getInstance</code>
     * is used.
     * 
     * @return the Calendar.
     * @see #setCalendar(Calendar)
     */
    private Calendar getCalendar() {

        // Makes sure we have an calendar instance
        if (calendar == null) {
            calendar = Calendar.getInstance();
            // Start by a zeroed calendar to avoid having values for lower
            // resolution variables e.g. time when resolution is day
            for (Resolution r : Resolution.getResolutionsLowerThan(resolution)) {
                calendar.set(r.getCalendarField(), 0);
            }
            calendar.set(Calendar.MILLISECOND, 0);
        }

        // Clone the instance
        final Calendar newCal = (Calendar) calendar.clone();

        // Assigns the current time tom calendar.
        final Date currentDate = getValue();
        if (currentDate != null) {
            newCal.setTime(currentDate);
        }

        final TimeZone currentTimeZone = getTimeZone();
        if (currentTimeZone != null) {
            newCal.setTimeZone(currentTimeZone);
        }

        return newCal;
    }

    /**
     * Sets formatting used by some component implementations. See
     * {@link SimpleDateFormat} for format details.
     * 
     * By default it is encouraged to used default formatting defined by Locale,
     * but due some JVM bugs it is sometimes necessary to use this method to
     * override formatting. See Vaadin issue #2200.
     * 
     * @param dateFormat
     *            the dateFormat to set
     * 
     * @see com.vaadin.ui.AbstractComponent#setLocale(Locale))
     */
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
        markAsDirty();
    }

    /**
     * Returns a format string used to format date value on client side or null
     * if default formatting from {@link Component#getLocale()} is used.
     * 
     * @return the dateFormat
     */
    public String getDateFormat() {
        return dateFormat;
    }

    /**
     * Specifies whether or not date/time interpretation in component is to be
     * lenient.
     * 
     * @see Calendar#setLenient(boolean)
     * @see #isLenient()
     * 
     * @param lenient
     *            true if the lenient mode is to be turned on; false if it is to
     *            be turned off.
     */
    public void setLenient(boolean lenient) {
        this.lenient = lenient;
        markAsDirty();
    }

    /**
     * Returns whether date/time interpretation is to be lenient.
     * 
     * @see #setLenient(boolean)
     * 
     * @return true if the interpretation mode of this calendar is lenient;
     *         false otherwise.
     */
    public boolean isLenient() {
        return lenient;
    }

    @Override
    public void addListener(FocusListener listener) {
        addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
                FocusListener.focusMethod);
    }

    @Override
    public void removeListener(FocusListener listener) {
        removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
    }

    @Override
    public void addListener(BlurListener listener) {
        addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
                BlurListener.blurMethod);
    }

    @Override
    public void removeListener(BlurListener listener) {
        removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
    }

    /**
     * Checks whether ISO 8601 week numbers are shown in the date selector.
     * 
     * @return true if week numbers are shown, false otherwise.
     */
    public boolean isShowISOWeekNumbers() {
        return showISOWeekNumbers;
    }

    /**
     * Sets the visibility of ISO 8601 week numbers in the date selector. ISO
     * 8601 defines that a week always starts with a Monday so the week numbers
     * are only shown if this is the case.
     * 
     * @param showWeekNumbers
     *            true if week numbers should be shown, false otherwise.
     */
    public void setShowISOWeekNumbers(boolean showWeekNumbers) {
        showISOWeekNumbers = showWeekNumbers;
        markAsDirty();
    }

    /**
     * Validates the current value against registered validators if the field is
     * not empty. Note that DateField is considered empty (value == null) and
     * invalid if it contains text typed in by the user that couldn't be parsed
     * into a Date value.
     * 
     * @see com.vaadin.ui.AbstractField#validate()
     */
    @Override
    public void validate() throws InvalidValueException {
        /*
         * To work properly in form we must throw exception if there is
         * currently a parsing error in the datefield. Parsing error is kind of
         * an internal validator.
         */
        if (!uiHasValidDateString) {
            throw new UnparsableDateString(currentParseErrorMessage);
        }
        super.validate();
    }

    /**
     * Return the error message that is shown if the user inputted value can't
     * be parsed into a Date object. If
     * {@link #handleUnparsableDateString(String)} is overridden and it throws a
     * custom exception, the message returned by
     * {@link Exception#getLocalizedMessage()} will be used instead of the value
     * returned by this method.
     * 
     * @see #setParseErrorMessage(String)
     * 
     * @return the error message that the DateField uses when it can't parse the
     *         textual input from user to a Date object
     */
    public String getParseErrorMessage() {
        return defaultParseErrorMessage;
    }

    /**
     * Sets the default error message used if the DateField cannot parse the
     * text input by user to a Date field. Note that if the
     * {@link #handleUnparsableDateString(String)} method is overridden, the
     * localized message from its exception is used.
     * 
     * @see #getParseErrorMessage()
     * @see #handleUnparsableDateString(String)
     * @param parsingErrorMessage
     */
    public void setParseErrorMessage(String parsingErrorMessage) {
        defaultParseErrorMessage = parsingErrorMessage;
    }

    /**
     * Sets the time zone used by this date field. The time zone is used to
     * convert the absolute time in a Date object to a logical time displayed in
     * the selector and to convert the select time back to a Date object.
     * 
     * If no time zone has been set, the current default time zone returned by
     * {@code TimeZone.getDefault()} is used.
     * 
     * @see #getTimeZone()
     * @param timeZone
     *            the time zone to use for time calculations.
     */
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
        markAsDirty();
    }

    /**
     * Gets the time zone used by this field. The time zone is used to convert
     * the absolute time in a Date object to a logical time displayed in the
     * selector and to convert the select time back to a Date object.
     * 
     * If {@code null} is returned, the current default time zone returned by
     * {@code TimeZone.getDefault()} is used.
     * 
     * @return the current time zone
     */
    public TimeZone getTimeZone() {
        return timeZone;
    }

    public static class UnparsableDateString extends
            Validator.InvalidValueException {

        public UnparsableDateString(String message) {
            super(message);
        }

    }
}