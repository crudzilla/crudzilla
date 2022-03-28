package io.github.crudzilla;

import io.github.crudzilla.persistency.CRUDZillaEntity;

public interface CRUDZillaForm<E extends CRUDZillaEntity<K>, K>{
    K getId();
}
