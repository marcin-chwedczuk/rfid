package pl.marcinchwedczuk.rfid.card.commons.utils;

public class ToStringBuilder {
    private final StringBuilder result = new StringBuilder();

    private boolean first = true;
    private String cached = null;

    public ToStringBuilder(Class<?> forType) {
        result.append(forType.getSimpleName())
                .append(" {");
    }

    public ToStringBuilder appendField(String fieldName, Object value) {
        appendSeparator();
        result.append(String.format("%s: %s", fieldName, value));
        return this;
    }

    public ToStringBuilder appendField(String fieldName, String value) {
        appendSeparator();
        result.append(String.format("%s: '%s'",
                fieldName, StringUtils.escape(value)));
        return this;
    }

    private void appendSeparator() {
        if (!first) {
            result.append(", ");
        } else {
            result.append(" ");
        }

        first = false;
    }

    public String toString() {
        if (cached == null) {
            result.append(" }");
            cached = result.toString();
        }
        return cached;
    }
}
