package studio.magemonkey.genesis.addon.playershops.objects;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.misc.InputReader;
import studio.magemonkey.genesis.misc.userinput.GenesisUserInput;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerShopsUserInputPrice extends GenesisUserInput {
    private final PlayerShop shop;
    private final UUID       uuid;
    private final ItemStack  item;
    private final int        slot;

    public void receivedInput(Player p, String text) {
        if (p.getUniqueId() == uuid) { //probably it is not even possible this event will trigger with an other player

            if (p.getInventory().getItem(slot) == null) {
                ClassManager.manager.getBugFinder()
                        .warn("Unable to set price of PlayerShops item: The given slot somehow is empty.");
                return;
            }

            if (!p.getInventory().getItem(slot).equals(item)) {
                ClassManager.manager.getBugFinder()
                        .warn("Unable to set price of PlayerShops item: The given slot somehow contains a different item than it did before the price was entered.");
                return;
            }

            text = ChatColor.stripColor(text).trim();
            double worth = InputReader.getDouble(text, -1);
            if (worth == -1) {
                ClassManager.manager.getMessageHandler()
                        .sendMessageDirect(ClassManager.manager.getStringManager()
                                .transform(shop.getPlugin()
                                        .getMessages()
                                        .get("Message.InvalidNumber")
                                        .replace("%input%", text), p), p);
                openInventorySync(p);
                return;
            }

            if (worth > shop.getPlugin().getSettings().getPriceMax()) {
                ClassManager.manager.getMessageHandler()
                        .sendMessageDirect(ClassManager.manager.getStringManager()
                                .transform(shop.getPlugin()
                                        .getMessages()
                                        .get("Message.InvalidNumberHigh")
                                        .replace("%input%", text), p), p);
                openInventorySync(p);
                return;
            }

            if (worth < shop.getPlugin().getSettings().getPriceMin()) {
                ClassManager.manager.getMessageHandler()
                        .sendMessageDirect(ClassManager.manager.getStringManager()
                                .transform(shop.getPlugin()
                                        .getMessages()
                                        .get("Message.InvalidNumberLow")
                                        .replace("%input%", text), p), p);
                openInventorySync(p);
                return;
            }

            p.getInventory().setItem(slot, null);

            shop.addItem(new PlayerShopItem(item, item.getAmount(), worth));

            openInventorySync(p);
        }
    }

    private void openInventorySync(final Player p) {
        Bukkit.getScheduler().runTask(shop.getPlugin(), () -> shop.getShopEdit().openInventory(p));
    }
}
