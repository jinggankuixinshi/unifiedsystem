package com.unified.common.core;

import java.util.List;

public interface BaseService<T> {

    T getById(Long id);

    List<T> list();

    boolean save(T entity);

    boolean updateById(T entity);

    boolean removeById(Long id);
}
