package domuscontrol.houses;

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
public class DivisionInfo {

    private final String houseName;
    private final String divisionName;
    private final int deviceCount;

    /**
     * Constructs a DivisionInfo with the given house name, division name and device count.
     *
     * @param houseName     The name of the house the division belongs to.
     * @param divisionName  The name of the division.
     * @param deviceCount   The number of devices in the division.
     */
    public DivisionInfo(String houseName, String divisionName, int deviceCount) {
        this.houseName = houseName;
        this.divisionName = divisionName;
        this.deviceCount = deviceCount;
    }

    /**
     * Returns the name of the house this division belongs to.
     *
     * @return The house name.
     */
    public String getHouseName() {
        return houseName;
    }

    /**
     * Returns the name of the division.
     *
     * @return The division name.
     */
    public String getDivisionName() {
        return divisionName;
    }

    /**
     * Returns the number of devices in this division.
     *
     * @return The device count.
     */
    public int getDeviceCount() {
        return deviceCount;
    }
}
