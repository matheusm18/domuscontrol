package domuscontrol.houses;

import java.io.Serializable;
import java.util.Objects;

/**
 * Data Transfer Object returned by division-related queries on the model facade.
 *
 * Because divisions are represented internally as String keys inside a House,
 * there is no standalone Division object to return. When a query needs to surface
 * information about a division to the view - particularly in cross-house statistics -
 * it must bundle the division name together with its house context and device count.
 * This class provides that grouping in an immutable, behaviour-free container.
 *
 * Instances are produced exclusively by DomusControl query methods and consumed
 * by the UI layer for display purposes only.
 *
 * @author Afonso Barros (a112178)
 * @author Martim Monteiro (a111013)
 * @author Matheus Azevedo (a111430)
 * @version 1.0
 */
public class DivisionInfo implements Serializable {

    /** The ID of the house this division belongs to. */
    private final int houseId;
    /** The name of the house this division belongs to. */
    private final String houseName;
    /** The name of the division. */
    private final String divisionName;
    /** The number of devices in this division. */
    private final int deviceCount;

    /**
     * Constructs a DivisionInfo with default values.
     */
    public DivisionInfo() {
        this.houseId = 0;
        this.houseName = "";
        this.divisionName = "";
        this.deviceCount = 0;
    }

    /**
     * Constructs a DivisionInfo with the given house ID, house name, division name, and device count.
     *
     * @param houseId      the ID of the house the division belongs to
     * @param houseName    the name of the house the division belongs to
     * @param divisionName the name of the division
     * @param deviceCount  the number of devices in the division
     */
    public DivisionInfo(int houseId, String houseName, String divisionName, int deviceCount) {
        this.houseId = houseId;
        this.houseName = houseName;
        this.divisionName = divisionName;
        this.deviceCount = deviceCount;
    }

    /**
     * Constructs a DivisionInfo by copying another DivisionInfo.
     *
     * @param other the DivisionInfo to copy
     */
    public DivisionInfo(DivisionInfo other) {
        this.houseId = other.houseId;
        this.houseName = other.houseName;
        this.divisionName = other.divisionName;
        this.deviceCount = other.deviceCount;
    }

    /**
     * Returns the ID of the house this division belongs to.
     *
     * @return the house ID
     */
    public int getHouseId() {
        return this.houseId;
    }

    /**
     * Returns the name of the house this division belongs to.
     *
     * @return the house name
     */
    public String getHouseName() {
        return this.houseName;
    }

    /**
     * Returns the name of the division.
     *
     * @return the division name
     */
    public String getDivisionName() {
        return this.divisionName;
    }

    /**
     * Returns the number of devices in this division.
     *
     * @return the device count
     */
    public int getDeviceCount() {
        return this.deviceCount;
    }

    /**
     * Creates a copy of this division info.
     *
     * @return a new DivisionInfo with the same values
     */
    @Override
    public DivisionInfo clone() {
        return new DivisionInfo(this);
    }

    /**
     * Compares this division info with another object for equality.
     *
     * @param o the object to compare with
     * @return true if all values match, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        DivisionInfo other = (DivisionInfo) o;
        return this.houseId == other.houseId
            && this.deviceCount == other.deviceCount
            && Objects.equals(this.houseName, other.houseName)
            && Objects.equals(this.divisionName, other.divisionName);
    }

    /**
     * Generates a hash code for this division info.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.houseId, this.houseName, this.divisionName, this.deviceCount);
    }

    /**
     * Returns a string representation of this division info.
     *
     * @return a formatted string with the division info values
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DivisionInfo { ")
          .append("houseId: ").append(this.houseId)
          .append(", houseName: ").append(this.houseName)
          .append(", divisionName: ").append(this.divisionName)
          .append(", deviceCount: ").append(this.deviceCount)
          .append(" }");
        return sb.toString();
    }
}