package net.labyfy.component.player.serializer;

/**
 * A serializer which serialized or deserialized objects.
 *
 * @param <S> The type to serialize or deserialize
 * @param <D> The type to serialize or deserialize
 */
public interface Serializer<S, D> {

    /**
     * Deserializes the given {@link S} type to the {@link D} type
     *
     * @param value The {@link S} being deserialized
     * @return a deserialized {@link D}
     */
    D deserialize(S value);

    /**
     * Serializes the given {@link D} type to the {@link S} type
     *
     * @param value The {@link D} being serialized
     * @return a serialized {@link S}
     */
    S serialize(D value);

}
