// package com.cashmallow.api.interfaces.authme.dto;
//
// import com.cashmallow.api.domain.model.traveler.GlobalCertification;
// import com.cashmallow.api.domain.model.traveler.Traveler;
// import lombok.Getter;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.StringUtils;
// import org.jetbrains.annotations.NotNull;
//
// import java.util.Arrays;
// import java.util.List;
// import java.util.stream.Collectors;
//
// import static com.cashmallow.api.domain.model.traveler.Traveler.TravelerSex.MALE;
// import static com.cashmallow.common.CommonUtil.textToNormalize;
//
// @Slf4j
// @Getter
// @RequiredArgsConstructor
// public class AuthmeTraveler {
//
//     private final GlobalCertification.CertificationFileType certificationType;
//     private final String dateOfBirth; // 2000-03-31 , user table
//     // 여권 정보
//     private final String passportIssueDate;
//     private final String passportExpDate;
//     private final String passportCountry;
//     private final String localFirstName;
//     private final String localLastName;
//     private final String identificationNumber;
//     private final Traveler.TravelerSex sex;
//     private final String certificationPhoto;
//     private final String base64Photo;
//     private final String address; // residentCard, driverLicense
//     private final String nationality; // residentCard
//     private final String periodOfStay; // residentCard
//     private final String restrictionEmployment; // residentCard
//     private final String visaStatus; // residentCard
//     private final String authmeJson;
//     private final String facePhoto;
//     private final String base64FacePhoto;
//     private final String customerId;
//
//     public static AuthmeTraveler of(AuthmeTraveler authme,
//                                     AuthMeCustomerWebhookResponse authMeWebhook,
//                                     String authMeWebhookJson) {
//         AuthMeCustomerResponseDocumentDetails details = authMeWebhook.data().verification().details();
//         return new AuthmeTraveler(
//                 authme.certificationType,
//                 details.dateOfBirth(),
//                 details.dateOfIssue(),
//                 details.expiryDate(),
//                 details.country(),
//                 getFirstName(details.name()),
//                 getLastName(details.name()),
//                 details.documentNumber(),
//                 getGender(details.gender()),
//                 authme.certificationPhoto,
//                 authme.base64Photo,
//                 details.address(),
//                 details.nationality(),
//                 details.periodOfStay(),
//                 details.restrictionEmployment(),
//                 details.visaStatus(),
//                 authMeWebhookJson,
//                 authme.facePhoto,
//                 authme.base64FacePhoto,
//                 authme.customerId
//         );
//     }
//
//     public Traveler.TravelerSex getSex() {
//         return sex == null ? MALE : sex;
//     }
//
//     // for HK, JP
//     public static AuthmeTraveler idCard(AuthMeCustomerResponseDocumentDetails details,
//                                         String fileName,
//                                         String base64Image,
//                                         String authmeJson,
//                                         String facePhoto,
//                                         String base64FacePhoto,
//                                         String customerId,
//                                         boolean isJP) {
//         return new AuthmeTraveler(
//                 GlobalCertification.CertificationFileType.ID_CARD,
//                 getDateOfBirth(details.dateOfBirth()),
//                 isJP ? null : details.dateOfIssue(),
//                 isJP ? null : details.expiryDate(),
//                 isJP ? null : details.country(),
//                 isJP ? null : getFirstName(details.name()),
//                 isJP ? null : getLastName(details.name()),
//                 isJP ? details.idNumber() : details.documentNumber(),
//                 getGender(details.gender()),
//                 fileName,
//                 base64Image,
//                 details.address(),
//                 isJP ? null : details.nationality(),
//                 isJP ? null : details.periodOfStay(),
//                 isJP ? null : details.restrictionEmployment(),
//                 isJP ? null : details.visaStatus(),
//                 authmeJson,
//                 facePhoto,
//                 base64FacePhoto,
//                 customerId
//         );
//     }
//
//     // for HK
//     // 확인 완료
//     public static AuthmeTraveler passport(AuthMeCustomerResponseDocumentDetails details,
//                                           String fileName,
//                                           String base64Image,
//                                           String authmeJson,
//                                           String facePhoto,
//                                           String base64FacePhoto,
//                                           String customerId) {
//         return new AuthmeTraveler(
//                 GlobalCertification.CertificationFileType.PASSPORT,
//                 getDateOfBirth(details.dateOfBirth()),
//                 details.dateOfIssue(), // null임
//                 details.expiryDate(),
//                 details.country(), // CHN
//                 details.givenName(),
//                 details.surname(),
//                 details.documentNumber(),
//                 getGender(details.gender()),
//                 fileName,
//                 base64Image,
//                 details.address(),
//                 details.nationality(),
//                 details.periodOfStay(),
//                 details.restrictionEmployment(),
//                 details.visaStatus(),
//                 authmeJson,
//                 facePhoto,
//                 base64FacePhoto,
//                 customerId
//         );
//     }
//
//     // for JP
//     // 확인 완료
//     public static AuthmeTraveler residentCard(AuthMeCustomerResponseDocumentDetails details,
//                                               String fileName,
//                                               String base64Image,
//                                               String authmeJson,
//                                               String facePhoto,
//                                               String base64FacePhoto,
//                                               String customerId) {
//         return new AuthmeTraveler(
//                 GlobalCertification.CertificationFileType.RESIDENCE_CARD,
//                 getDateOfBirth(details.dateOfBirth()),
//                 details.dateOfIssue(),
//                 details.expiryDate(),
//                 null,
//                 getFirstName(details.name()),
//                 getLastName(details.name()),
//                 details.documentNumber(),
//                 getGender(details.gender()),
//                 fileName,
//                 base64Image,
//                 details.address(),
//                 details.nationality(),
//                 details.periodOfStay(),
//                 details.restrictionEmployment(),
//                 details.visaStatus(),
//                 authmeJson,
//                 facePhoto,
//                 base64FacePhoto,
//                 customerId
//         );
//     }
//
//     // for JP
//     public static AuthmeTraveler myNumberCard(AuthMeCustomerResponseDocumentDetails details,
//                                               String fileName,
//                                               String base64Image,
//                                               String authmeJson,
//                                               String facePhoto,
//                                               String base64FacePhoto,
//                                               String customerId) {
//         return new AuthmeTraveler(
//                 GlobalCertification.CertificationFileType.ID_CARD, // JP 전용
//                 getDateOfBirth(details.dateOfBirth()),
//                 details.dateOfIssue(),
//                 details.expiryDate(),
//                 details.country(),
//                 getFirstName(details.name()),
//                 getLastName(details.name()),
//                 details.documentNumber(),
//                 getGender(details.gender()),
//                 fileName,
//                 base64Image,
//                 details.address(),
//                 details.nationality(),
//                 details.periodOfStay(),
//                 details.restrictionEmployment(),
//                 details.visaStatus(),
//                 authmeJson,
//                 facePhoto,
//                 base64FacePhoto,
//                 customerId
//         );
//     }
//
//     // for JP
//     public static AuthmeTraveler driverLicense(AuthMeCustomerResponseDocumentDetails details,
//                                                String fileName,
//                                                String base64Image,
//                                                String authmeJson,
//                                                String facePhoto,
//                                                String base64FacePhoto,
//                                                String customerId) {
//         return new AuthmeTraveler(
//                 GlobalCertification.CertificationFileType.DRIVER_LICENSE,
//                 getDateOfBirth(details.dateOfBirth()),
//                 details.dateOfIssue(),
//                 details.expiryDate(),
//                 null,
//                 null,
//                 null,
//                 details.documentNumber(),
//                 null,
//                 fileName,
//                 base64Image,
//                 details.address(),
//                 null,
//                 null,
//                 null,
//                 null,
//                 authmeJson,
//                 facePhoto,
//                 base64FacePhoto,
//                 customerId
//         );
//     }
//
//     private static @NotNull String getDateOfBirth(String datedOfBirth) {
//         if (StringUtils.isNoneBlank(datedOfBirth)) {
//             return datedOfBirth.replaceAll("-", "");
//         }
//         return datedOfBirth;
//     }
//
//     // 성
//     public static String getFirstName(String localName) {
//         return getName(localName).firstName();
//     }
//
//     // 이름
//     public static String getLastName(String localName) {
//         return getName(localName).lastName();
//     }
//
//     public static AuthmeName getName(String localName) {
//         if (StringUtils.isBlank(localName.trim())) {
//             return new AuthmeName("", "");
//         }
//
//         localName = textToNormalize(localName);
//
//         try {
//             List<String> list = Arrays.asList(localName.split(" |,"));
//             String firstName = list.stream().skip(1).map(String::trim).map(String::toUpperCase).collect(Collectors.joining(" "));
//             String lastName = list.get(0).toUpperCase();
//             return new AuthmeName(firstName, lastName);
//         } catch (Exception e) {
//             log.error("e : {}", e.getMessage());
//         }
//         return new AuthmeName("", "");
//     }
//
//
//     public static Traveler.TravelerSex getGender(String gender) {
//         if (StringUtils.isBlank(gender)) {
//             return null;
//         }
//
//         if ("Male".equalsIgnoreCase(gender) || "M".equalsIgnoreCase(gender) || gender.equalsIgnoreCase("男")) {
//             return MALE;
//         } else if ("Female".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender) || gender.equalsIgnoreCase("女")) {
//             return Traveler.TravelerSex.FEMALE;
//         }
//
//         return null;
//     }
//
//     public String getAddress() {
//         return textToNormalize(address);
//     }
//
//
// }
