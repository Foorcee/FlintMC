package net.labyfy.component.player.world;

import net.labyfy.component.player.Player;

import java.util.List;

/**
 * Represents a world
 */
public interface World {

    /**
     * Retrieves the time of this world.
     *
     * @return the time of this world.
     */
    long getTime();

    /**
     * Retrieves the player count of this world.
     *
     * @return the player count of this world.
     */
    int getPlayerCount();

    /**
     * Retrieves a collection with all players of this world.
     *
     * @return a collection with all players of this world.
     */
    List<Player> getPlayers();

    /**
     * Retrieves the dimension of this world.
     *
     * @return the dimension of this world.
     */
    Dimension getDimension();

}
