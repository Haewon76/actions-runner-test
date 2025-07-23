package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.shared.Const;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResultVO {
    private List<Object> content;

    private Integer page;
    private Integer size;
    private String sort;

    private Integer totalElements;
    private Integer totalPages;
    private Integer number;
    private Integer numberOfElements;

    private Boolean first;
    private Boolean last;

    public List<Object> getContent() {
        return content;
    }

    public void setContent(List<Object> content) {
        this.content = content;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public Integer getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Integer totalElements) {
        this.totalElements = totalElements;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Boolean isFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Boolean isLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }


    public SearchResultVO() {
        this.content = null;

        this.page = Const.DEF_PAGE_NO;
        this.size = Const.DEF_PAGE_SIZE;
        this.sort = "";

        this.totalElements = 0;
        this.totalPages = 0;
        this.number = 0;
        this.numberOfElements = 0;

        this.first = true;
        this.last = true;
    }

    // 기능:
    public SearchResultVO(Integer page, Integer size, String sort) {

        if (page == null || page < 0) {
            page = 0;
        }

        if (size == null || size < 1) {
            size = 1;
        }

        this.page = page;
        this.size = size;
        this.sort = sort;

        this.totalElements = 0;
        this.totalPages = 0;
        this.number = 0;
        this.numberOfElements = 0;

        this.first = true;
        this.last = true;
    }

    // 기능:
    public void setResult(List<Object> content, Integer totalElements, Integer curPage) {
        this.content = content;
        this.totalElements = totalElements;
        this.totalPages = (totalElements + size - 1) / size;
        this.first = curPage == 0;
        this.last = curPage >= (this.totalPages - 1);
        this.number = curPage;
        this.numberOfElements = content == null ? 0 : content.size();
    }

    public static SearchResultVO jsonToObjct(String jsonStr) {
        SearchResultVO vo = null;

        try {
            ObjectMapper mapper = new ObjectMapper();
            vo = mapper.readValue(jsonStr, SearchResultVO.class);
        } catch (JsonParseException e) {
            log.error(e.getMessage(), e);
        } catch (JsonMappingException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        return vo;
    }

}
