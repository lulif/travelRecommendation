package com.gdxx.dao;

import java.util.List;

import com.gdxx.entity.Sites;
/*
 * 地点Dao接口
 */
public interface SitesDao {
	List<Sites> querySitesList();

	int querySitesCount();

	Sites querySiteById(Long siteId);

}
