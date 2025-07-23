package com.cashmallow.api.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
@AllArgsConstructor
@Getter
public enum Job {
    // version 1
    BUSINESS("Business", "회사원", "商業", "会社員", false, "001"),
    FREELANCER("Freelancer", "프리랜서", "自由工作者", "フリーランス", true, "002"),
    TEACHER("Teacher", "교사", "教師", "教師", false, "003"),
    CONSTRUCTION_INDUSTRY("Construction industry", "건축업 종사자", "建築業", "建築業従事者", false, "004"),
    REAL_ESTATE_INDUSTRY("Real estate industry", "부동산업 종사자", "不動產業", "不動産業従事者", false, "005"),
    MEDICAL_INDUSTRY("Medical industry", "의료 전문직", "醫療業", "医療専門職", false, "006"),

    // version 2
    CLERK("Clerk", "사무직", "行政人員、文員", "事務職員", true, "007"),
    PUBLIC_ADMINISTRATION("Public administration", "공무원", "公務員", "公務員", true, "008"),
    EDUCATION("Education", "교육업", "教育業", "教育", true, "009"),
    SOCIAL_WORK_AND_HEALTHCARE_ACTIVITIES("Social work and healthcare activities", "의료 및 사회복지사", "社工、醫療業", "医療、社会福祉", true, "010"),
    ARTS_AND_ENTERTAINMENT("Arts and entertainment", "예술, 엔터테인먼트업", "藝術、娛樂業", "芸術、エンターテインメント", true, "011"),
    SERVICE_INDUSTRY("Service industry", "서비스업", "服務業", "サービス業", true, "012"),
    HOTEL_AND_CATERING_INDUSTRY("Hotel and catering industry", "숙박·음식업", "酒店餐飲業", "宿泊業，飲食サービス業", true, "013"),
    TRADING_WHOLESALE_AND_RETAIL_TRADES("Trading, wholesale and retail trades", "수출입, 도매, 소매업", "貿易、批發及零售業", "貿易、卸売業，小売業", true, "014"),
    PRECIOUS_METAL_INDUSTRY("Precious metal industry", "귀금속업", "貴金屬業", "貴金属・宝石製品製造業", true, "015"),
    FINANCIAL_INDUSTRY("Financial industry", "금융업", "金融業", "金融", true, "016"),
    FOREIGN_EXCHANGE_INDUSTRY("Foreign exchange industry", "외환업", "外匯業", "為替", true, "017"),
    GAMBLING_INDUSTRY("Gambling industry", "카지노업", "博弈業", "カジノ", true, "018"),
    LENDING_INDUSTRY("Lending industry", "대부업", "借貸業", "貸金業", true, "019"),
    INSURANCE_INDUSTRY("Insurance industry", "보험업", "保險業", "保険", true, "020"),
    REAL_ESTATE_ACTIVITIES("Real estate activities", "부동산업", "地產活動", "不動産", true, "021"),
    LEGAL_INDUSTRY("Legal industry", "법률업", "法律業", "法律", true, "022"),
    MANUFACTURING("Manufacturing", "제조업", "製造業", "製造", true, "023"),
    CONSTRUCTION("Construction", "건축업", "建築業", "建築", true, "024"),
    INFORMATION_AND_COMMUNICATIONS("Information and communications", "정보통신업", "資訊及通訊業", "情報通信業", true, "025"),
    TRANSIT_STORAGE_POSTAL("Transit , storage, postal", "운수창고업", "運輸、倉儲、郵政業", "運輸業、倉庫業", true, "026"),
    ACADEMIC_RESEARCH_EDUCATION("Academic research, education", "학술, 교육업", "學術研究、教育業", "学術,教育業", true, "027"),
    HYDROPOWER_COAL_ENERGY_INDUSTRY("Hydropower Coal Energy Industry", "전기·가스·수도·에너지 업종", "水電煤環衛業", "電気・ガズー・水道・エネルギー", true, "028"),
    AGRICULTURE_FORESTRY_FISHERY("Agriculture, Forestry, Fishery", "농업, 임업, 어업, 묵축업, 광업", "農林漁業", "農業、林業、漁業", true, "029"),
    NON_PROFIT_ORGANIZATIONS("Non-profit organizations", "비영리단체", "非營利組織", "非営利的団体", true, "030"),
    STUDENT("Student", "학생", "學生", "学生", true, "031"),
    SOLDIER("Soldier", "군인", "軍人", "軍人", true, "032"),
    INOCCUPATION("Inoccupation", "무직", "無業", "無職", true, "033"),
    OTHER("Other", "기타", "其他", "その他", false, "034"),
    UNKNOWN("---", "---", "---", "---", false, "035");
    final String english;
    final String korean;
    final String chinese;
    final String japan;
    final boolean use;
    final String octaJobCode;
    static final List<JobDto> jobsEn = Arrays.stream(Job.values()).filter(Job::isUse).map(j -> new JobDto(j.name(), j.getEnglish())).toList();
    static final List<JobDto> jobsKo = Arrays.stream(Job.values()).filter(Job::isUse).map(j -> new JobDto(j.name(), j.getKorean())).toList();
    static final List<JobDto> jobsZh = Arrays.stream(Job.values()).filter(Job::isUse).map(j -> new JobDto(j.name(), j.getChinese())).toList();
    static final List<JobDto> jobsJa = Arrays.stream(Job.values()).filter(Job::isUse).map(j -> new JobDto(j.name(), j.getJapan())).toList();
    public static List<JobDto> getJobs(Locale locale) {
        if (locale == null) {
            return jobsEn;
        }
        return switch (locale.getLanguage()) {
            case "ko" -> jobsKo;
            case "zh" -> jobsZh;
            case "ja" -> jobsJa;
            default -> jobsEn;
        };
    }
    public String getTitle(Locale locale) {
        if (locale == null) {
            return this.getEnglish();
        }
        return switch (locale.getLanguage()) {
            case "ko" -> this.getKorean();
            case "zh" -> this.getChinese();
            case "ja" -> this.getJapan();
            default -> this.getEnglish();
        };
    }
    public static String octaJobCodes() {
        return Arrays.stream(Job.values()).map(m -> m.getKorean() + "(" + m.getOctaJobCode() + ")").collect(Collectors.joining("\n"));
    }
    @AllArgsConstructor
    @Getter
    public static class JobDto {
        String key;
        String jobTitle;
    }
}