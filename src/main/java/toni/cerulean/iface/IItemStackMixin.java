package toni.cerulean.iface;

/**
 * Interface for accessing custom fields of ItemStack
 */
public interface IItemStackMixin {
    /**
     * Save the previous stack size, before inventory change.
     * @param value previous value
     */
    void cerulean$setPreviousStackSize(int value);

    /**
     * Get the previous stack size, before inventory change.
     * @return previous value
     */
    int cerulean$getPreviousStackSize();
}
