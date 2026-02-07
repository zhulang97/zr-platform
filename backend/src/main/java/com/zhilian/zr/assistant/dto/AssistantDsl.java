package com.zhilian.zr.assistant.dto;

import java.util.List;

public class AssistantDsl {
  private String intent;
  private Filters filters;
  private List<Sort> sort;
  private Page page;

  public static class Filters {
    private String nameLike;
    private String idNo;
    private String disabilityCardNo;
    private List<Long> districtIds;
    private List<Long> streetIds;
    private List<String> disabilityCategories;
    private List<String> disabilityLevels;
    private Boolean hasCar;
    private Boolean hasMedicalSubsidy;
    private Boolean hasPensionSubsidy;
    private Boolean hasBlindCard;
    private List<String> cardStatus;
    private List<String> subsidyStatus;

    public String getNameLike() { return nameLike; }
    public void setNameLike(String nameLike) { this.nameLike = nameLike; }
    public String getIdNo() { return idNo; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public String getDisabilityCardNo() { return disabilityCardNo; }
    public void setDisabilityCardNo(String disabilityCardNo) { this.disabilityCardNo = disabilityCardNo; }
    public List<Long> getDistrictIds() { return districtIds; }
    public void setDistrictIds(List<Long> districtIds) { this.districtIds = districtIds; }
    public List<Long> getStreetIds() { return streetIds; }
    public void setStreetIds(List<Long> streetIds) { this.streetIds = streetIds; }
    public List<String> getDisabilityCategories() { return disabilityCategories; }
    public void setDisabilityCategories(List<String> disabilityCategories) { this.disabilityCategories = disabilityCategories; }
    public List<String> getDisabilityLevels() { return disabilityLevels; }
    public void setDisabilityLevels(List<String> disabilityLevels) { this.disabilityLevels = disabilityLevels; }
    public Boolean getHasCar() { return hasCar; }
    public void setHasCar(Boolean hasCar) { this.hasCar = hasCar; }
    public Boolean getHasMedicalSubsidy() { return hasMedicalSubsidy; }
    public void setHasMedicalSubsidy(Boolean hasMedicalSubsidy) { this.hasMedicalSubsidy = hasMedicalSubsidy; }
    public Boolean getHasPensionSubsidy() { return hasPensionSubsidy; }
    public void setHasPensionSubsidy(Boolean hasPensionSubsidy) { this.hasPensionSubsidy = hasPensionSubsidy; }
    public Boolean getHasBlindCard() { return hasBlindCard; }
    public void setHasBlindCard(Boolean hasBlindCard) { this.hasBlindCard = hasBlindCard; }
    public List<String> getCardStatus() { return cardStatus; }
    public void setCardStatus(List<String> cardStatus) { this.cardStatus = cardStatus; }
    public List<String> getSubsidyStatus() { return subsidyStatus; }
    public void setSubsidyStatus(List<String> subsidyStatus) { this.subsidyStatus = subsidyStatus; }
  }

  public static class Sort {
    private String field;
    private String order;

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }
    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
  }

  public static class Page {
    private int no = 1;
    private int size = 20;

    public int getNo() { return no; }
    public void setNo(int no) { this.no = no; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
  }

  public String getIntent() { return intent; }
  public void setIntent(String intent) { this.intent = intent; }
  public Filters getFilters() { return filters; }
  public void setFilters(Filters filters) { this.filters = filters; }
  public List<Sort> getSort() { return sort; }
  public void setSort(List<Sort> sort) { this.sort = sort; }
  public Page getPage() { return page; }
  public void setPage(Page page) { this.page = page; }
}