package com.argentinaflags.block;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Bandera decorativa. Un mismo bloque sirve para dos usos:
 * wall=false: mastil sobre el suelo (o sobre segmentos de mastil); wall=true: bandera colgada de una barra en una pared.
 * FACING es la direccion hacia la que mira la cara frontal de la bandera.
 *
 * La tela se dibuja mas grande que el bloque (se extiende hacia los lados); las formas de seleccion
 * se recortan al bloque. Para subir la bandera, hacer clic derecho sobre ella con un mastil en la mano.
 */
public class FlagBlock extends Block implements EntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    // Formas para FACING = north (se rotan para las otras direcciones).
    private static final VoxelShape FLOOR_BASE = Shapes.or(
            Block.box(7, 0, 7, 9, 16, 9),
            Block.box(0, 2.5, 7.75, 5, 16, 8.25),
            Block.box(5, 14, 7.75, 7, 15, 8.25),
            Block.box(5, 4, 7.75, 7, 5, 8.25));
    private static final VoxelShape FLOOR_COLLISION_BASE = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape WALL_BASE = Shapes.or(
            Block.box(0, 13, 13, 16, 14, 14),
            Block.box(1, 13, 14, 2, 14, 16),
            Block.box(14, 13, 14, 15, 14, 16),
            Block.box(0, 0, 12.5, 16, 13, 13));

    private static final Map<Direction, VoxelShape> FLOOR_SHAPES = rotations(FLOOR_BASE);
    private static final Map<Direction, VoxelShape> FLOOR_COLLISIONS = rotations(FLOOR_COLLISION_BASE);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = rotations(WALL_BASE);

    private final String textureId;
    private final boolean large;

    /**
     * @param textureId id de la bandera base (nombre del PNG en textures/block)
     * @param large     true para la version grande (4 veces el area de la normal)
     */
    public FlagBlock(BlockBehaviour.Properties properties, String textureId, boolean large) {
        super(properties);
        this.textureId = textureId;
        this.large = large;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WALL, false));
    }

    public String getTextureId() {
        return textureId;
    }

    public boolean isLarge() {
        return large;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FlagBlockEntity(pos, state);
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
            // Clic en el costado de un bloque: bandera colgada en la pared.
            state = defaultBlockState().setValue(WALL, true).setValue(FACING, clickedFace);
        } else {
            // Clic arriba de un bloque: bandera en mastil sobre el suelo.
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
        BlockState belowState = level.getBlockState(below);
        return belowState.getBlock() instanceof PoleBlock || belowState.isFaceSturdy(level, below, Direction.UP);
    }

    /** Clic derecho con un mastil: la bandera sube un bloque y su posicion actual pasa a ser mastil. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!state.getValue(WALL) && stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof PoleBlock) {
            BlockPos above = pos.above();
            if (level.isOutsideBuildHeight(above) || !level.getBlockState(above).canBeReplaced()) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                level.setBlock(pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(above, state, Block.UPDATE_ALL);
                level.playSound(null, pos, SoundType.WOOD.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 0.8F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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
