package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.shared.Const;

public class BankAccountAskVO {
    private String country; // 일치 조회, 국가
    private Integer companyId; // 일치 조회, 지사 ID.
    private String bankCode; // LIKE 조회, 은행코드 (공통코드 참조)
    private String bankName; // LIKE 조회, 은행명 (공통코드 참조)
    private String bankAccountNo; // 일치 조회, 계좌번호
    private String useYn; // 일치 조회, 사용 여부

    private Integer startRow;
    private Integer page;
    private Integer size;
    private String sort;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }


    public String getUseYn() {
        return useYn;
    }

    public void setUseYn(String useYn) {
        this.useYn = useYn;
    }

    public Integer getStartRow() {
        return startRow;
    }

    public void setStartRow(Integer startRow) {
        this.startRow = startRow;
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

    public BankAccountAskVO() {
        this.page = Const.DEF_PAGE_NO;
        this.size = Const.DEF_PAGE_SIZE;
    }

    @Override
    public String toString() {
        return "country" + country
                + ", company_id:" + companyId
                + ", use_yn:" + useYn

                + ", start_row:" + startRow
                + ", page:" + page
                + ", size:" + size
                + ", sort:" + sort;
    }

    public String toString(String prefix) {
        return prefix + toString();
    }

    public void setPageInfo(int page, int size, String sort) {
        this.page = page;
        this.size = size;
        this.sort = sort;
    }

    //    public HashMap<String, String> MakeParams() {
    //        HashMap<String, String> params = new HashMap<String, String>();
    //
    //        CommUtil.setParam(params, "country", country);
    //        CommUtil.setParam(params, "company_id", company_id);
    //        CommUtil.setParam(params, "use_yn", use_yn);
    //
    //        CommUtil.setParam(params, "page", page);
    //        CommUtil.setParam(params, "size", size);
    //        CommUtil.setParam(params, "sort", sort);
    //        return params;
    //    }

    public boolean checkValidation() {
        return true;
    }

}
