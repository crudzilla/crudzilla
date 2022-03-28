package io.github.crudzilla.persistency;

public interface CRUDZillaEntity<K> {
    K getId();
    String getLabel();

    default boolean isActive(){ return true; }
    default void setActive(boolean isActive){ }
}