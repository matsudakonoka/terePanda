/**  
 * @Title: RestPageImpl.java
 * @Package com.cnpc.epai.core.dataset.util
 * @Description: 分页工具类
 * @author cuijiaqi
 * @date 2018年5月7日 下午2:34:16 
 * {修改记录：cuijiaqi 2018年5月7日 下午2:34:16}
*/
package com.cnpc.epai.core.worktask.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;


public class RestPageImpl<T> extends PageImpl<T>{

	private static final long serialVersionUID = 1L;

	/**
	 * 构造方法
	 * @param content
	 * @param page
	 * @param size
	 * @param total
	 */
	@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public RestPageImpl(@JsonProperty("content") List<T> content,
                        @JsonProperty("number") int page,
                        @JsonProperty("size") int size,
                        @JsonProperty("totalElements") long total) {
        super(content, new PageRequest(page, size), total);
    }

    /**
     * 构造方法
     * @param content
     * @param pageable
     * @param total
     */
    public RestPageImpl(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    /**
     * 构造方法
     * @param content
     */
    public RestPageImpl(List<T> content) {
        super(content);
    }

    /**
     * 构造方法
     */
    public RestPageImpl() {
        super(new ArrayList<T>());
    }
}