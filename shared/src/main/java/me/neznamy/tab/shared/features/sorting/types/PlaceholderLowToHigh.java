package me.neznamy.tab.shared.features.sorting.types;

import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.sorting.Sorting;

/**
 * Sorting by a numeric placeholder from lowest to highest
 */
public class PlaceholderLowToHigh extends SortingType {

    /**
     * Constructs new instance with given parameter
     *
     * @param   sortingPlaceholder
     *          placeholder to sort by
     */
    public PlaceholderLowToHigh(Sorting sorting, String sortingPlaceholder) {
        super(sorting, "PLACEHOLDER_LOW_TO_HIGH", sortingPlaceholder);
    }

    @Override
    public String getChars(TabPlayer p) {
        String output = setPlaceholders(p);
        sorting.setTeamNameNote(p, sorting.getTeamNameNote(p) + "\n-> " + sortingPlaceholder + " returned \"&e" + output + "&r\". &r");
        return compressNumber(p, DEFAULT_NUMBER + parseDouble(sortingPlaceholder, output, 0, p));
    }
}