package com.argentinaflags.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bandera decorativa. Un mismo bloque sirve para dos usos:
 * <ul>
 *   <li>wall=false: mastil clavado en el suelo (se apoya sobre la cara superior de un bloque).</li>
 *   <li>wall=true: bandera colgada de una barra pegada a una pared (se apoya en el bloque de atras).</li>
 * </ul>
 * FACING es la direccion hacia la que mira la cara frontal de la bandera.
 */
public class FlagBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    // Formas para FACING = north (se rotan para las otras direcciones).
    private static final VoxelShape FLOOR_BASE = Shapes.or(
            Block.box(13, 0, 7, 15, 16, 9),
            Block.box(0, 7.3, 7.75, 12, 15, 8.25),
            Block.box(12, 14, 7.75, 13, 15, 8.25),
            Block.box(12, 8, 7.75, 13, 9, 8.25));
    private static final VoxelShape FLOOR_COLLISION_BASE = Block.box(13, 0, 7, 15, 16, 9);
    private static final VoxelShape WALL_BASE = Shapes.or(
            Block.box(0.5, 13, 13, 15.5, 14, 14),
            Block.box(1, 13, 14, 2, 14, 16),
            Block.box(14, 13, 14, 15, 14, 16),
            Block.box(1, 4, 12.5, 15, 13, 13));

    private static final Map<Direction, VoxelShape> FLOOR_SHAPES = rotations(FLOOR_BASE);
    private static final Map<Direction, VoxelShape> FLOOR_COLLISIONS = rotations(FLOOR_COLLISION_BASE);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = rotations(WALL_BASE);

    public FlagBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WALL, false));
    }

    public static BlockBehaviour.Properties defaultProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.WOOL)
                .strength(0.5F)
                .sound(SoundType.WOOL)
                .noOcclusion()
                .pushReaction(PushReaction.DESTROY);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        BlockState state;
        if (clickedFace.getAxis().isHorizontal()) {
            // Se hizo clic en el costado de un bloque: bandera colgada en la pared.
            state = defaultBlockState().setValue(WALL, true).setValue(FACING, clickedFace);
        } else {
            // Se hizo clic arriba de un bloque: bandera en mastil sobre el suelo.
            state = defaultBlockState().setValue(WALL, false)
                    .setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        return state.canSurvive(level, pos) ? state : null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(WALL)) {
            Direction facing = state.getValue(FACING);
            BlockPos supportPos = pos.relative(facing.getOpposite());
            return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing);
        }
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return state.getValue(WALL) ? WALL_SHAPES.get(facing) : FLOOR_SHAPES.get(facing);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // Solo el mastil tiene colision; la tela y las banderas de pared se atraviesan.
        return state.getValue(WALL) ? Shapes.empty() : FLOOR_COLLISIONS.get(state.getValue(FACING));
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // ---- Utilidades de formas ----

    private static Map<Direction, VoxelShape> rotations(VoxelShape north) {
        Map<Direction, VoxelShape> map = new EnumMap<>(Direction.class);
        VoxelShape shape = north;
        for (Direction direction : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            map.put(direction, shape);
            shape = rotateClockwise(shape);
        }
        return map;
    }

    /** Gira una forma 90 grados en sentido horario (visto desde arriba) alrededor del centro del bloque. */
    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                result[0] = Shapes.or(result[0], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
        return result[0];
    }
}
