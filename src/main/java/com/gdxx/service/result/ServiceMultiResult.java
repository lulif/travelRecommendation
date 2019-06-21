package com.gdxx.service.result;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/*
 * service层数据封装
 */
@Setter
@Getter
@AllArgsConstructor
public class ServiceMultiResult<T> {
	private int total;
	private List<T> result;
}
