package com.scrm.r2dbc.plus.function;

import java.io.Serializable;
import java.util.function.Function;

/**
 * 支持序列化的 Function 接口，用于 Lambda 表达式
 * 
 * @param <T> 输入类型
 * @param <R> 返回类型
 * @author dason
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}
