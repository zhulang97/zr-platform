package com.zhilian.zr.person.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("T_PERSON_INDEX")
public class PersonIndexEntity {
    
    @TableId(value = "ID_CARD", type = IdType.INPUT)
    private String idCard;
    
    private String name;
    private String gender;
    private Integer age;
    private String phone;
    private String district;
    private String street;
    private String committee;
    private String contactAddress;
    private String householdAddress;
    private String disabilityCategory;
    private String disabilityLevel;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String locationStatus;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    
    public String getCommittee() { return committee; }
    public void setCommittee(String committee) { this.committee = committee; }
    
    public String getContactAddress() { return contactAddress; }
    public void setContactAddress(String contactAddress) { this.contactAddress = contactAddress; }
    
    public String getHouseholdAddress() { return householdAddress; }
    public void setHouseholdAddress(String householdAddress) { this.householdAddress = householdAddress; }
    
    public String getDisabilityCategory() { return disabilityCategory; }
    public void setDisabilityCategory(String disabilityCategory) { this.disabilityCategory = disabilityCategory; }
    
    public String getDisabilityLevel() { return disabilityLevel; }
    public void setDisabilityLevel(String disabilityLevel) { this.disabilityLevel = disabilityLevel; }
    
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    
    public String getLocationStatus() { return locationStatus; }
    public void setLocationStatus(String locationStatus) { this.locationStatus = locationStatus; }
    
    public LocalDateTime getFirstSeenAt() { return firstSeenAt; }
    public void setFirstSeenAt(LocalDateTime firstSeenAt) { this.firstSeenAt = firstSeenAt; }
    
    public LocalDateTime getLastSeenAt() { return lastSeenAt; }
    public void setLastSeenAt(LocalDateTime lastSeenAt) { this.lastSeenAt = lastSeenAt; }
}
