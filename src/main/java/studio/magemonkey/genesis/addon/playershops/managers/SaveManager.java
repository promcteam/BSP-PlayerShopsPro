package studio.magemonkey.genesis.addon.playershops.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.Genesis;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.core.GenesisShop;

import java.util.UUID;

@RequiredArgsConstructor
public class SaveManager {
    private final PlayerShops plugin;

    public PlayerShop loadShop(UUID id, Player p, REASON_LOAD reason, boolean openShop, boolean updateListing) {
        PlayerShop shop = plugin.getShopsManager().getPlayerShop(id);

        if (shop != null) {
            Genesis.debug("tried to load playershop of" + shop.getOwnerName() + " because of " + reason.name()
                    + " but failed because it was already loaded");
            return shop;
        }

        switch (reason) {
            case FIRST_CREATION:
                shop = new PlayerShop(plugin, p);
                break;
            case OWNER_JOIN:
                shop = new PlayerShop(plugin, id);
                break;
            case SERVER_START:
                shop = new PlayerShop(plugin, id);
                Player owner = Bukkit.getPlayer(id);
                if (owner != null) {
                    shop.ownerJoin(p);
                }
                break;
            case SERVER_START_RENTING:
                shop = new PlayerShop(plugin, id);
                break;
            case SERVER_START_OWNER_ONLINE:
                shop = new PlayerShop(plugin, id);
                shop.ownerJoin(p);
                break;
        }

        Genesis.debug("loaded shop of " + shop.getOwnerName() + " because of " + reason.name());
        plugin.getShopsManager().addPlayerShop(shop);

        if (shop != null) {
            shop.createShop();
            if (updateListing) {
                plugin.getShopsManager().updateShopListing();
            }
            if (openShop) {
                shop.getShop().openInventory(p);
            }
        }

        return shop;
    }


    public void saveShop(PlayerShop shop, Player p, REASON_SAVE reason) {
        GenesisShop s;
        switch (reason) {
            case EDITMODE_FINISH:
                shop.finishEdit(true);
                if (shop.getShop() != null) {
                    shop.getShop().openInventory(p);
                }
                break;
            case EDITMODE_OWNER_QUIT:
                shop.save();
                if (shop.getRentTimeLeft(false, false) < 0 && plugin.getSettings().getListOnlinePlayersOnly()) {
                    s = shop.getCurrentShop();
                    if (s != null) {
                        s.close();
                    }
                    shop.unload();
                }
                break;
            case SERVER_UNLOAD:
                s = shop.getCurrentShop();
                if (s != null) {
                    s.close();
                }
                shop.save();
                break;
            case WORLD_SAVE:
                shop.save();
                break;
            case OWNER_QUIT:
                shop.save();
                if (shop.getRentTimeLeft(false, false) < 0 && plugin.getSettings().getListOnlinePlayersOnly()) {
                    s = shop.getCurrentShop();
                    if (s != null) {
                        s.close();
                    }
                    shop.unload();
                }
            case SERVER_RELOAD:
                s = shop.getCurrentShop();
                if (s != null) {
                    s.close();
                }
                shop.save();
                break;
        }
        Genesis.debug("saving shop of " + shop.getOwnerName() + " because of " + reason.name());
    }


    @Getter
    public enum REASON_SAVE {
        EDITMODE_OWNER_QUIT(true),
        EDITMODE_FINISH(false),
        OWNER_QUIT(true),
        WORLD_SAVE(false),
        SERVER_UNLOAD(true),
        SERVER_RELOAD(false);

        private final boolean ownerQuit;

        REASON_SAVE(boolean owner_quit) {
            this.ownerQuit = owner_quit;
        }
    }

    @Getter
    public enum REASON_LOAD {
        FIRST_CREATION(false),
        OWNER_JOIN(true),
        SERVER_START(false),
        SERVER_START_RENTING(false),
        SERVER_START_OWNER_ONLINE(true); //Reason of loading is onliner being online. Does not mean this is always called when owner is online and shop loaded.

        private final boolean ownerJoin;

        REASON_LOAD(boolean owner_join) {
            this.ownerJoin = owner_join;
        }
    }

}
