package com.zhilian.zr.person.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

public final class PersonDtos {
  private PersonDtos() {
  }

  public record PersonSearchRequest(
      String nameLike,
      String idNo,
      String disabilityCardNo,
      List<Long> districtIds,
      List<Long> streetIds,
      List<String> disabilityCategories,
      List<String> disabilityLevels,
      Boolean hasCar,
      Boolean hasMedicalSubsidy,
      Boolean hasPensionSubsidy,
      Boolean hasBlindCard,
      List<String> cardStatus,
      List<String> subsidyStatus,
      @Min(1) int pageNo,
      @Min(1) @Max(200) int pageSize
  ) {
  }

  public record PersonRow(
      long personId,
      String nameMasked,
      String idNoMasked,
      String disabilityCategoryCode,
      String disabilityCategoryName,
      String disabilityLevelCode,
      String disabilityLevelName,
      String district,
      String street,
      String cardStatusCode,
      String cardStatusName,
      boolean hasCar,
      boolean hasMedicalSubsidy,
      boolean hasPensionSubsidy,
      boolean riskFlag
  ) {
  }

  public record PersonDetail(
      String nameMasked,
      String idNoMasked,
      String disabilityCardNo,
      String disabilityCategoryCode,
      String disabilityCategoryName,
      String disabilityLevelCode,
      String disabilityLevelName,
      String issueDate,
      String cardStatusCode,
      String cardStatusName
  ) {
  }
}
