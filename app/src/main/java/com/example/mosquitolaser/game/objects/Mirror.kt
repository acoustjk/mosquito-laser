package com.example.mosquitolaser.game.objects

import android.graphics.RectF

/**
 * Represents a mirror that can reflect laser beams.
 *
 * Mirrors are defined in normalized (0-1) coordinate space and can optionally
 * be rotated by the player within a defined angular range.
 *
 * @param id        Unique identifier for this mirror.
 * @param x         Normalized horizontal center position.
 * @param y         Normalized vertical center position.
 * @param angle     Current surface angle in degrees (e.g. 45° = diagonal mirror).
 * @param minAngle  Minimum allowed rotation angle in degrees.
 * @param maxAngle  Maximum allowed rotation angle in degrees.
 * @param isMovable Whether the player is allowed to rotate this mirror.
 * @param size      Normalized half-size of the mirror's bounding box (default 0.06).
 */
class Mirror(
    val id: Int,
    var x: Float,
    var y: Float,
    var angle: Float,
    val minAngle: Float,
    val maxAngle: Float,
    val isMovable: Boolean,
    val size: Float = 0.06f
) {
    /**
     * Calculates the outgoing laser angle after reflection off this mirror.
     *
     * In 2D, for a mirror whose surface lies along angle M degrees, the law of
     * reflection gives:
     *   reflected = 2 * M - incoming   (mod 360)
     *
     * Derivation (screen coords, y-axis points DOWN):
     *   d_r = d - 2*(d·n̂)*n̂  →  angle(d_r) = 2*M - A
     *
     * Examples (y-down screen coords):
     *   M=45°,  A=0°   (right)  → R=90°  (down)  ✓
     *   M=135°, A=0°   (right)  → R=270° (up)    ✓
     *   M=45°,  A=90°  (down)   → R=0°   (right) ✓
     *   M=135°, A=90°  (down)   → R=180° (left)  ✓
     *
     * @param incomingAngle The direction the laser is travelling, in degrees.
     * @return The reflected direction in degrees, normalised to [0, 360).
     */
    fun reflect(incomingAngle: Float): Float {
        val reflected = 2f * angle - incomingAngle
        return ((reflected % 360f) + 360f) % 360f
    }

    /**
     * Determines whether a touch point (in pixel space) falls within the
     * mirror's axis-aligned bounding rectangle.
     *
     * @param px      Touch X coordinate in pixels.
     * @param py      Touch Y coordinate in pixels.
     * @param canvasW Canvas width in pixels.
     * @param canvasH Canvas height in pixels.
     * @return `true` if the touch is inside the mirror bounds.
     */
    fun containsPoint(px: Float, py: Float, canvasW: Float, canvasH: Float): Boolean {
        return toPixelRect(canvasW, canvasH).contains(px, py)
    }

    /**
     * Returns the mirror's bounding rectangle in pixel coordinates.
     *
     * The mirror is centred at (x, y) with half-extents of [size] in each axis.
     *
     * @param canvasW Canvas width in pixels.
     * @param canvasH Canvas height in pixels.
     * @return A [RectF] representing the mirror bounds in pixels.
     */
    fun toPixelRect(canvasW: Float, canvasH: Float): RectF {
        val cx = x * canvasW
        val cy = y * canvasH
        val halfW = size * canvasW
        val halfH = size * canvasH
        return RectF(cx - halfW, cy - halfH, cx + halfW, cy + halfH)
    }

    /**
     * Clamps [angle] to the allowed rotation range [[minAngle], [maxAngle]].
     * Call this after modifying [angle] externally to enforce constraints.
     */
    fun clampAngle() {
        angle = angle.coerceIn(minAngle, maxAngle)
    }
}
