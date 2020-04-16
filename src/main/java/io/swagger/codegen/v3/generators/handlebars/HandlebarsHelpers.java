package io.swagger.codegen.v3.generators.handlebars;

import com.github.jknack.handlebars.Options;
import com.google.common.base.Objects;

import java.io.IOException;

public class HandlebarsHelpers {

  public static final String OPERATOR_EQUAL = "==";
  public static final String OPERATOR_NOT_EQUAL = "!=";
  public static final String OPERATOR_LESS = "<";
  public static final String OPERATOR_LESS_OR_EQUAL = "<=";
  public static final String OPERATOR_GREATER = ">";
  public static final String OPERATOR_GREATER_OR_EQUAL = ">=";
  public static final String OPERATOR_AND = "&&";
  public static final String OPERATOR_OR = "||";

  public CharSequence equals(final Object element, final Options options) throws IOException {
    Object element2 = options.param(0);
    return Objects.equal(element, element2) ? options.fn() : options.inverse();
  }

  public CharSequence notEquals(final Object element, final Options options) throws IOException {
    Object element2 = options.param(0);
    return Objects.equal(element, element2) ? options.inverse() : options.fn();
  }

  public CharSequence defaultStatus(final Object element, final Options options) throws IOException {
    return ("0".equals(element) || "default".equals(element)) ? options.fn() : options.inverse();
  }

  public CharSequence ifCond(final Object element, final Options options) throws IOException {
    String operator = options.param(0).toString();
    Object element2 = options.param(1);

    // Equal operators
    switch (operator) {
      case OPERATOR_EQUAL:
        return (element == element2) ? options.fn(this) : options.inverse(this);
      case OPERATOR_NOT_EQUAL:
        return (element != element2) ? options.fn(this) : options.inverse(this);
    }

    // Integer operators
    if (OPERATOR_LESS.equals(operator)
        || OPERATOR_LESS_OR_EQUAL.equals(operator)
        || OPERATOR_GREATER.equals(operator)
        || OPERATOR_GREATER_OR_EQUAL.equals(operator)
    ) {
      int intElement = (int) element;
      int intElement2 = (int) element2;

      switch (operator) {
        case OPERATOR_LESS:
          return (intElement < intElement2) ? options.fn(this) : options.inverse(this);
        case OPERATOR_LESS_OR_EQUAL:
          return (intElement <= intElement2) ? options.fn(this) : options.inverse(this);
        case OPERATOR_GREATER:
          return (intElement > intElement2) ? options.fn(this) : options.inverse(this);
        case OPERATOR_GREATER_OR_EQUAL:
          return (intElement >= intElement2) ? options.fn(this) : options.inverse(this);
      }
    }

    // Boolean operators
    if (OPERATOR_AND.equals(operator) || OPERATOR_OR.equals(operator)) {
      boolean boolElement;
      if (element instanceof String) {
        boolElement = ((String) element).isEmpty();
      } else {
        boolElement = (Boolean) element;
      }

      boolean boolElement2;
      if (element2 instanceof String) {
        boolElement2 = ((String) element2).isEmpty();
      } else {
        boolElement2 = (Boolean) element2;
      }

      switch (operator) {
        case OPERATOR_AND:
          return (boolElement && boolElement2) ? options.fn(this) : options.inverse(this);
        case OPERATOR_OR:
          return (boolElement || boolElement2) ? options.fn(this) : options.inverse(this);
      }
    }

    // Default
    return options.inverse(this);
  }

}
