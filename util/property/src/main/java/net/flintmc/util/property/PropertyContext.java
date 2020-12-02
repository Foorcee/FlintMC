package net.flintmc.util.property;

import java.util.Map;

public interface PropertyContext<
    T_PropertyContextAware extends PropertyContextAware<T_PropertyContextAware>> {

  /**
   * @param property          the property to modify on this instance
   * @param propertyValue     the value to set the property to
   * @param <T_PropertyValue> @see {@link Property< T_PropertyValue >}
   * @param <T_PropertyMeta>  @see {@link Property<T_PropertyMeta>}
   * @return this
   */
  <T_PropertyValue, T_PropertyMeta> T_PropertyContextAware setPropertyValue(
      Property<T_PropertyValue, T_PropertyMeta> property, T_PropertyValue propertyValue);

  /**
   * @param property          the property to modify on this instance
   * @param propertyMode      the mode to set the property to
   * @param <T_PropertyValue> @see {@link Property<T_PropertyValue>}
   * @param <T_PropertyMeta>  @see {@link Property<T_PropertyMeta>}
   * @return this
   */
  <T_PropertyValue, T_PropertyMeta> T_PropertyContextAware setPropertyMeta(
      Property<T_PropertyValue, T_PropertyMeta> property, T_PropertyMeta propertyMode);

  /**
   * @param property          the property to get the value from
   * @param <T_PropertyValue> @see {@link Property<T_PropertyValue>}
   * @param <T_PropertyMeta>  @see {@link Property<T_PropertyMeta>}
   * @return the current value of the given property
   */
  <T_PropertyValue, T_PropertyMeta> T_PropertyValue getPropertyValue(
      Property<T_PropertyValue, T_PropertyMeta> property);

  /**
   * @param property          the property to get the mode from
   * @param <T_PropertyValue> @see {@link Property<T_PropertyValue>}
   * @param <T_PropertyMeta>  @see {@link Property<T_PropertyMeta>}
   * @return the current mode of the given property
   */
  <T_PropertyValue, T_PropertyMeta> T_PropertyMeta getPropertyMeta(
      Property<T_PropertyValue, T_PropertyMeta> property);

  Map<Property<?, ?>, Object> getProperties();

  interface Factory {
    <T_PropertyContextAware extends PropertyContextAware<T_PropertyContextAware>>
    PropertyContext<T_PropertyContextAware> create(T_PropertyContextAware propertyContextAware);
  }
}
