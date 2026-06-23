package tech.jnkr.presume;

sealed interface PrimitiveDraw permits BooleanDraw, IntDraw, FloatDraw, DoubleDraw, LongDraw {}

record BooleanDraw(boolean value) implements PrimitiveDraw {}

record IntDraw(int value) implements PrimitiveDraw {}

record FloatDraw(float value) implements PrimitiveDraw {}

record DoubleDraw(double value) implements PrimitiveDraw {}

record LongDraw(long value) implements PrimitiveDraw {}
