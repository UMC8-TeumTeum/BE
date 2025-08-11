package umc.teumteum.server.domain.home.entity.enums;

public enum LocationType {
  HOUSE(1, "집"),       // 1, 집
  SCHOOL(2, "학교"),    // 2, 학교
  WORK(3, "회사"), // 3, 회사
  ING(4, "이동중"),      // 4, 이동중
  OUTDOOR(5, "실외"),   // 5, 실외
  INDOOR(6, "실내");    // 6, 실내

  private final int locationId;
  private final String displayName;

  LocationType(int locationId, String displayName) {
    this.locationId = locationId;
    this.displayName = displayName;
  }

  public int getLocationId() {
    return locationId;
  }

  public String getDisplayName() {
    return displayName;
  }
}
