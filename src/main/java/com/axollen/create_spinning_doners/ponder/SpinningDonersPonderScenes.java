package com.axollen.create_spinning_doners.ponder;

import com.axollen.create_spinning_doners.block.CookingStationBlock;
import com.axollen.create_spinning_doners.block.DonerSpinnerBlock;
import com.axollen.create_spinning_doners.block.SidewaysHeaterBlock;
import com.axollen.create_spinning_doners.registry.ModBlocks;
import com.axollen.create_spinning_doners.registry.ModItems;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.scene.PonderStoryBoard;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public class SpinningDonersPonderScenes {

	public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper) {
		helper.addStoryBoard(ModBlocks.DONER_SPINNER.getId(), "doner_spinner", DONER_SPINNER,
				ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
		helper.addStoryBoard(ModBlocks.DONER_SPINNER.getId(), "doner_spinner_cooking", DONER_SPINNER_AUTOMATION,
				ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
		helper.addStoryBoard(ModBlocks.DONER_SPINNER.getId(), "doner_spinner_cooking", DONER_SPINNER_FACTORY,
				ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
		helper.addStoryBoard(ModBlocks.SIDEWAYS_HEATER.getId(), "sideways_heater", SIDEWAYS_HEATER,
				ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
		helper.addStoryBoard(ModBlocks.COOKING_STATION.getId(), "doner_spinner_cooking", COOKING_STATION,
				ResourceLocation.fromNamespaceAndPath("create", "kinetic_relays"));
	}

	static void checkerboardFloor(SceneBuilder scene, SceneBuildingUtil util) {
		for (int x = 0; x < 5; x++) {
			for (int z = 0; z < 5; z++) {
				boolean white = (x + z) % 2 == 0;
				BlockState state = (white ? Blocks.WHITE_CONCRETE : Blocks.SNOW_BLOCK).defaultBlockState();
				scene.world().setBlock(util.grid().at(x, 0, z), state, false);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static BlockState setExtracting(BlockState state, boolean extracting) {
		Optional<Property<?>> property = state.getProperties().stream()
				.filter(p -> p.getName().equals("extracting") && p instanceof BooleanProperty)
				.findFirst();
		if (property.isPresent()) {
			return state.setValue((BooleanProperty) property.get(), extracting);
		}
		return state;
	}

	@SuppressWarnings("unchecked")
	private static BlockState setDirection(BlockState state, Direction direction) {
		Optional<Property<?>> property = state.getProperties().stream()
				.filter(p -> (p.getName().equals("facing") || p.getName().equals("horizontal_facing")) && p instanceof DirectionProperty)
				.findFirst();
		if (property.isPresent()) {
			return state.setValue((DirectionProperty) property.get(), direction);
		}
		return state;
	}

	public static final PonderStoryBoard DONER_SPINNER = (scene, util) -> {
		scene.title("doner_spinner", "The Doner Spinner");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		checkerboardFloor(scene, util);

		BlockPos motorPos = util.grid().at(2, 0, 2);
		BlockPos spinnerLower = util.grid().at(2, 1, 2);
		BlockPos spinnerUpper = util.grid().at(2, 2, 2);

		scene.world().setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(BlockStateProperties.FACING, Direction.UP), false);
		scene.world().modifyBlockEntityNBT(util.select().position(motorPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);

		scene.overlay().showText(70)
				.text("create_spinning_doners.ponder.doner_spinner.text_1")
				.pointAt(util.vector().topOf(motorPos))
				.placeNearTarget();
		scene.idle(80);

		scene.world().setBlock(spinnerLower, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.LOWER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		scene.world().setBlock(spinnerUpper, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.UPPER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		
		scene.world().showSection(util.select().fromTo(spinnerLower, spinnerUpper), Direction.DOWN);
		scene.idle(20);

		scene.world().modifyBlockEntityNBT(util.select().fromTo(spinnerLower, spinnerUpper), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner.text_2")
				.pointAt(util.vector().centerOf(spinnerUpper))
				.placeNearTarget();
		
		scene.idle(20);
		scene.overlay().showControls(util.vector().topOf(spinnerUpper), Pointing.DOWN, 40)
				.rightClick()
				.withItem(ModItems.RAW_FULL_DONER.get().getDefaultInstance());
		scene.idle(45);

		scene.world().modifyBlockEntityNBT(util.select().position(spinnerLower), BlockEntity.class, nbt -> {
			nbt.putInt("DonerState", 1);
			nbt.putInt("Pieces", 50);
		});
		scene.world().modifyBlock(spinnerUpper, s -> s.setValue(DonerSpinnerBlock.HAS_DONER, true), false);
		scene.idle(30);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner.text_3")
				.pointAt(util.vector().centerOf(spinnerUpper))
				.placeNearTarget();
		
		scene.idle(20);
		scene.overlay().showControls(util.vector().topOf(spinnerUpper), Pointing.DOWN, 40)
				.rightClick()
				.withItem(Items.IRON_SWORD.getDefaultInstance());
		scene.idle(45);

		ItemStack piece = ModItems.RAW_DONER_PIECE.get().getDefaultInstance();
		scene.world().createItemEntity(util.vector().centerOf(spinnerUpper), util.vector().of(0.1, 0.1, 0), piece);
		scene.world().modifyBlockEntityNBT(util.select().position(spinnerLower), BlockEntity.class, nbt -> nbt.putInt("Pieces", 49));
		scene.idle(30);

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.doner_spinner.text_4")
				.pointAt(util.vector().centerOf(spinnerUpper))
				.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner.text_5")
				.pointAt(util.vector().blockSurface(spinnerLower, Direction.NORTH))
				.placeNearTarget();
		scene.idle(30);
		scene.overlay().showControls(util.vector().centerOf(spinnerLower), Pointing.RIGHT, 40).rightClick();
		scene.idle(80);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner.text_6")
				.pointAt(util.vector().centerOf(spinnerLower))
				.placeNearTarget();
		scene.idle(30);
		scene.overlay().showControls(util.vector().topOf(spinnerLower), Pointing.DOWN, 40).withItem(AllItems.GOGGLES.asStack());
		scene.idle(80);

		scene.markAsFinished();
	};

	public static final PonderStoryBoard DONER_SPINNER_AUTOMATION = (scene, util) -> {
		scene.title("doner_spinner_cooking", "Automating the Spinner");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		checkerboardFloor(scene, util);

		BlockPos motorPos = util.grid().at(2, 0, 2);
		BlockPos spinnerLower = util.grid().at(2, 1, 2);
		BlockPos spinnerUpper = util.grid().at(2, 2, 2);
		BlockPos leverPos = util.grid().at(3, 1, 2);

		scene.world().setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(BlockStateProperties.FACING, Direction.UP), false);
		scene.world().modifyBlockEntityNBT(util.select().position(motorPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));

		scene.world().setBlock(spinnerLower, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.LOWER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		scene.world().setBlock(spinnerUpper, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.UPPER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH)
				.setValue(DonerSpinnerBlock.HAS_DONER, true)
				.setValue(DonerSpinnerBlock.COOKED, true), false);
		
		scene.world().modifyBlockEntityNBT(util.select().fromTo(spinnerLower, spinnerUpper), BlockEntity.class, nbt -> {
			nbt.putInt("DonerState", 3);
			nbt.putInt("Pieces", 50);
			nbt.putFloat("Speed", 64.0f);
		});

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);
		scene.world().showSection(util.select().fromTo(motorPos, spinnerUpper), Direction.DOWN);
		scene.idle(20);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_1")
				.pointAt(util.vector().centerOf(spinnerLower))
				.placeNearTarget();
		
		scene.world().setBlock(leverPos, Blocks.LEVER.defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
				.setValue(BlockStateProperties.ATTACH_FACE, net.minecraft.world.level.block.state.properties.AttachFace.FLOOR), false);
		scene.world().showSection(util.select().position(leverPos), Direction.WEST);
		scene.idle(100);
		
		scene.world().toggleRedstonePower(util.select().position(leverPos));
		scene.effects().indicateRedstone(leverPos);
		scene.idle(20);

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_2")
				.pointAt(util.vector().centerOf(spinnerUpper))
				.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_3")
				.pointAt(util.vector().blockSurface(spinnerLower, Direction.EAST))
				.placeNearTarget();
		
		scene.idle(20);
		scene.overlay().showControls(util.vector().blockSurface(spinnerLower, Direction.EAST), Pointing.LEFT, 40)
				.scroll()
				.withItem(AllBlocks.COGWHEEL.asStack());
		scene.idle(90);

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_4")
				.pointAt(util.vector().blockSurface(spinnerLower, Direction.EAST))
				.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_5")
				.pointAt(util.vector().blockSurface(spinnerLower, Direction.NORTH))
				.placeNearTarget();
		scene.idle(30);
		scene.overlay().showControls(util.vector().blockSurface(spinnerLower, Direction.NORTH), Pointing.DOWN, 40).rightClick().withItem(AllItems.WRENCH.asStack());
		scene.idle(20);
		scene.world().modifyBlock(spinnerLower, s -> s.setValue(DonerSpinnerBlock.FRONT_OUTPUT, false), false);
		scene.idle(80);

		
		BlockPos inF = util.grid().at(2, 1, 1);
		BlockPos outF = util.grid().at(2, 1, 3);
		
		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_6")
				.pointAt(util.vector().centerOf(spinnerLower))
				.placeNearTarget();
		scene.idle(30);
		
		scene.world().setBlock(inF.below(), AllBlocks.BRASS_CASING.getDefaultState(), false);
		scene.world().setBlock(outF.below(), AllBlocks.BRASS_CASING.getDefaultState(), false);
		scene.world().showSection(util.select().fromTo(inF.below(), outF.below()), Direction.UP);
		scene.idle(10);

		scene.world().setBlock(inF, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.NORTH), false);
		scene.world().modifyBlock(inF, s -> setExtracting(s, false), false);
		scene.world().setBlock(outF, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.SOUTH), false);
		scene.world().modifyBlock(outF, s -> setExtracting(s, true), false);
		
		scene.world().showSection(util.select().position(inF), Direction.SOUTH);
		scene.world().showSection(util.select().position(outF), Direction.NORTH);
		scene.idle(50);

		scene.overlay().showText(90)
				.text("create_spinning_doners.ponder.doner_spinner_cooking.text_7")
				.pointAt(util.vector().blockSurface(spinnerLower, Direction.NORTH))
				.placeNearTarget();
		
		
		for (int i = 0; i < 3; i++) {
			scene.world().createItemEntity(util.vector().centerOf(outF).add(0, 0, 0.4), util.vector().of(0, 0, 0.1), ModItems.COOKED_DONER_PIECE.get().getDefaultInstance());
			scene.idle(40);
		}
		scene.idle(70);

		scene.markAsFinished();
	};

	public static final PonderStoryBoard DONER_SPINNER_FACTORY = (scene, util) -> {
		scene.title("doner_spinner_factory", "The Döner Factory");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		checkerboardFloor(scene, util);

		
		BlockPos motorPos = util.grid().at(2, 0, 2);
		scene.world().setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState().setValue(BlockStateProperties.FACING, Direction.UP), false);
		scene.world().modifyBlockEntityNBT(util.select().position(motorPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 128.0f));

		for (int x = 0; x < 5; x++) {
			if (x == 2) continue;
			scene.world().setBlock(util.grid().at(x, 0, 2), AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X), false);
		}

		for (int x = 1; x <= 3; x += 2) {
			scene.world().setBlock(util.grid().at(x, 0, 1), AllBlocks.ANDESITE_CASING.getDefaultState(), false);
			scene.world().setBlock(util.grid().at(x, 0, 3), AllBlocks.ANDESITE_CASING.getDefaultState(), false);
		}

		
		BlockPos s1L = util.grid().at(1, 1, 2);
		BlockPos s1U = util.grid().at(1, 2, 2);
		BlockPos s2L = util.grid().at(3, 1, 2);
		BlockPos s2U = util.grid().at(3, 2, 2);
		
		scene.world().setBlock(s1L, ModBlocks.DONER_SPINNER.get().defaultBlockState().setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.LOWER).setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		scene.world().setBlock(s1U, ModBlocks.DONER_SPINNER.get().defaultBlockState().setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.UPPER).setValue(DonerSpinnerBlock.FACING, Direction.NORTH).setValue(DonerSpinnerBlock.HAS_DONER, true).setValue(DonerSpinnerBlock.COOKED, true), false);
		scene.world().setBlock(s2L, ModBlocks.DONER_SPINNER.get().defaultBlockState().setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.LOWER).setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		scene.world().setBlock(s2U, ModBlocks.DONER_SPINNER.get().defaultBlockState().setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.UPPER).setValue(DonerSpinnerBlock.FACING, Direction.NORTH).setValue(DonerSpinnerBlock.HAS_DONER, true).setValue(DonerSpinnerBlock.COOKED, true), false);

		
		scene.world().setBlock(util.grid().at(0, 2, 2), ModBlocks.SIDEWAYS_HEATER.get().defaultBlockState().setValue(SidewaysHeaterBlock.FACING, Direction.EAST), false);
		scene.world().setBlock(util.grid().at(2, 2, 2), ModBlocks.SIDEWAYS_HEATER.get().defaultBlockState().setValue(SidewaysHeaterBlock.FACING, Direction.EAST), false);
		scene.world().setBlock(util.grid().at(4, 2, 2), ModBlocks.SIDEWAYS_HEATER.get().defaultBlockState().setValue(SidewaysHeaterBlock.FACING, Direction.WEST), false);

		
		scene.world().setBlock(util.grid().at(0, 1, 2), Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
		scene.world().setBlock(util.grid().at(2, 1, 2), Blocks.REDSTONE_BLOCK.defaultBlockState(), false);
		scene.world().setBlock(util.grid().at(4, 1, 2), Blocks.REDSTONE_BLOCK.defaultBlockState(), false);

		
		BlockPos inF1 = util.grid().at(1, 1, 1);
		BlockPos inF2 = util.grid().at(3, 1, 1);
		scene.world().setBlock(inF1, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.NORTH), false);
		scene.world().setBlock(inF2, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.NORTH), false);
		scene.world().modifyBlock(inF1, s -> setExtracting(s, false), false);
		scene.world().modifyBlock(inF2, s -> setExtracting(s, false), false);

		BlockPos outF1 = util.grid().at(1, 1, 3);
		BlockPos outF2 = util.grid().at(3, 1, 3);
		scene.world().setBlock(outF1, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.SOUTH), false);
		scene.world().setBlock(outF2, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.SOUTH), false);
		scene.world().modifyBlock(outF1, s -> setExtracting(s, true), false);
		scene.world().modifyBlock(outF2, s -> setExtracting(s, true), false);

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);
		scene.world().showSection(util.select().fromTo(0, 1, 0, 4, 2, 4), Direction.DOWN);
		scene.idle(20);

		scene.world().modifyBlockEntityNBT(util.select().fromTo(0, 0, 0, 4, 2, 4), BlockEntity.class, nbt -> nbt.putFloat("Speed", 128.0f));

		scene.world().modifyBlockEntityNBT(util.select().position(s1L), BlockEntity.class, nbt -> { nbt.putInt("DonerState", 3); nbt.putInt("Pieces", 50); });
		scene.world().modifyBlockEntityNBT(util.select().position(s2L), BlockEntity.class, nbt -> { nbt.putInt("DonerState", 3); nbt.putInt("Pieces", 50); });

		
		for (int i = 0; i < 20; i++) {
			if (i % 8 == 0) {
				scene.world().createItemEntity(util.vector().centerOf(inF1).add(0, 0, -0.4), util.vector().of(0, 0, 0.1), ModItems.RAW_FULL_DONER.get().getDefaultInstance());
			}
			if (i % 8 == 4) {
				scene.world().createItemEntity(util.vector().centerOf(inF2).add(0, 0, -0.4), util.vector().of(0, 0, 0.1), ModItems.RAW_FULL_DONER.get().getDefaultInstance());
			}
			
			if (i % 4 == 0) {
				BlockPos out = outF1;
				scene.world().createItemEntity(util.vector().centerOf(out).add(0, 0, 0.4), util.vector().of(0, 0, 0.2), ModItems.COOKED_DONER_PIECE.get().getDefaultInstance());
			}
			if (i % 4 == 2) {
				BlockPos out = outF2;
				scene.world().createItemEntity(util.vector().centerOf(out).add(0, 0, 0.4), util.vector().of(0, 0, 0.2), ModItems.COOKED_DONER_PIECE.get().getDefaultInstance());
			}
			
			scene.idle(35);
		}

		scene.markAsFinished();
	};

	public static final PonderStoryBoard SIDEWAYS_HEATER = (scene, util) -> {
		scene.title("sideways_heater", "The Sideways Heater");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		checkerboardFloor(scene, util);

		BlockPos heaterPos = util.grid().at(2, 2, 2);
		BlockPos motorPos = util.grid().at(2, 0, 2);
		BlockPos shaftPos = util.grid().at(2, 1, 2);
		BlockPos spinnerLower = util.grid().at(3, 1, 2);
		BlockPos spinnerUpper = util.grid().at(3, 2, 2);

		scene.world().setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(BlockStateProperties.FACING, Direction.UP), false);
		scene.world().modifyBlockEntityNBT(util.select().position(motorPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));

		scene.world().setBlock(shaftPos, AllBlocks.SHAFT.getDefaultState(), false);
		scene.world().modifyBlockEntityNBT(util.select().position(shaftPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));

		scene.world().setBlock(heaterPos, ModBlocks.SIDEWAYS_HEATER.get().defaultBlockState()
				.setValue(SidewaysHeaterBlock.FACING, Direction.EAST), false);
		scene.world().modifyBlockEntityNBT(util.select().position(heaterPos), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));
		
		scene.world().setBlock(spinnerLower, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.LOWER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH), false);
		scene.world().setBlock(spinnerUpper, ModBlocks.DONER_SPINNER.get().defaultBlockState()
				.setValue(DonerSpinnerBlock.HALF, DoubleBlockHalf.UPPER)
				.setValue(DonerSpinnerBlock.FACING, Direction.NORTH)
				.setValue(DonerSpinnerBlock.HAS_DONER, true), false);

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);
		scene.world().showSection(util.select().fromTo(motorPos, shaftPos), Direction.DOWN);
		scene.idle(10);
		scene.world().showSection(util.select().position(heaterPos), Direction.DOWN);
		scene.idle(20);

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.sideways_heater.text_1")
				.pointAt(util.vector().centerOf(heaterPos))
				.placeNearTarget();
		scene.idle(90);

		scene.world().showSection(util.select().fromTo(spinnerLower, spinnerUpper), Direction.WEST);
		scene.idle(20);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.sideways_heater.text_2")
				.pointAt(util.vector().blockSurface(heaterPos, Direction.EAST))
				.placeNearTarget();
		scene.idle(90);

		scene.world().modifyBlock(heaterPos, s -> s.setValue(SidewaysHeaterBlock.HEAT_LEVEL, 1), false);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.sideways_heater.text_3")
				.pointAt(util.vector().centerOf(heaterPos))
				.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.sideways_heater.text_4")
				.pointAt(util.vector().centerOf(spinnerUpper))
				.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.sideways_heater.text_5")
				.pointAt(util.vector().blockSurface(heaterPos, Direction.WEST))
				.placeNearTarget();
		scene.idle(20);
		scene.overlay().showControls(util.vector().blockSurface(heaterPos, Direction.WEST), Pointing.DOWN, 40).rightClick().withItem(AllItems.WRENCH.asStack());
		scene.idle(20);
		scene.world().modifyBlock(heaterPos, s -> s.setValue(SidewaysHeaterBlock.HAS_BACK_SHAFT, true), false);
		scene.idle(10);
		BlockPos backShaft = heaterPos.west();
		scene.world().setBlock(backShaft, AllBlocks.SHAFT.getDefaultState().setValue(BlockStateProperties.AXIS, Direction.Axis.X), false);
		scene.world().modifyBlockEntityNBT(util.select().position(backShaft), BlockEntity.class, nbt -> nbt.putFloat("Speed", 64.0f));
		scene.world().showSection(util.select().position(backShaft), Direction.EAST);
		scene.idle(80);

		scene.markAsFinished();
	};

	public static final PonderStoryBoard COOKING_STATION = (scene, util) -> {
		scene.title("cooking_station", "The Döner Station");
		scene.configureBasePlate(0, 0, 5);
		scene.showBasePlate();
		checkerboardFloor(scene, util);

		BlockPos motorPos = util.grid().at(2, 0, 2);
		BlockPos shaftWest = util.grid().at(1, 1, 2);
		BlockPos stationPos = util.grid().at(2, 1, 2);
		BlockPos shaftEast = util.grid().at(3, 1, 2);
		BlockPos inF = util.grid().at(2, 1, 1);
		BlockPos outF = util.grid().at(2, 1, 3);

		scene.world().setBlock(motorPos, AllBlocks.CREATIVE_MOTOR.getDefaultState()
				.setValue(BlockStateProperties.FACING, Direction.UP), false);
		scene.world().modifyBlockEntityNBT(util.select().position(motorPos), BlockEntity.class,
				nbt -> nbt.putFloat("Speed", 128.0f));

		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(10);

		scene.world().setBlock(shaftWest, AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.X), false);
		scene.world().setBlock(stationPos, ModBlocks.COOKING_STATION.get().defaultBlockState()
				.setValue(CookingStationBlock.FACING, Direction.NORTH)
				.setValue(CookingStationBlock.LEFT_SHAFT, true)
				.setValue(CookingStationBlock.RIGHT_SHAFT, true), false);
		scene.world().setBlock(shaftEast, AllBlocks.SHAFT.getDefaultState()
				.setValue(BlockStateProperties.AXIS, Direction.Axis.X), false);
		scene.world().showSection(util.select().fromTo(shaftWest, shaftEast), Direction.DOWN);
		scene.idle(20);
		scene.world().modifyBlockEntityNBT(util.select().fromTo(motorPos, shaftEast).add(util.select().position(shaftWest)), BlockEntity.class,
				nbt -> nbt.putFloat("Speed", 128.0f));

		scene.overlay().showText(80)
				.text("create_spinning_doners.ponder.cooking_station.text_1")
				.pointAt(util.vector().centerOf(stationPos))
				.placeNearTarget();
		scene.idle(90);

		scene.world().setBlock(inF.below(), AllBlocks.BRASS_CASING.getDefaultState(), false);
		scene.world().setBlock(outF.below(), AllBlocks.BRASS_CASING.getDefaultState(), false);
		scene.world().setBlock(inF, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.NORTH), false);
		scene.world().modifyBlock(inF, s -> setExtracting(s, false), false);
		scene.world().setBlock(outF, setDirection(AllBlocks.BRASS_FUNNEL.getDefaultState(), Direction.SOUTH), false);
		scene.world().modifyBlock(outF, s -> setExtracting(s, true), false);
		scene.world().showSection(util.select().fromTo(inF.below(), outF), Direction.UP);
		scene.idle(20);

		scene.overlay().showText(90)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.cooking_station.text_2")
				.pointAt(util.vector().centerOf(inF))
				.placeNearTarget();
		scene.idle(20);
		scene.world().createItemEntity(util.vector().centerOf(inF).add(0, 0, -0.4),
				util.vector().of(0, 0, 0.1), ModItems.RAW_FULL_DONER.get().getDefaultInstance());
		scene.idle(30);
		scene.world().modifyBlockEntityNBT(util.select().position(stationPos), BlockEntity.class, nbt -> {
			nbt.putInt("DonerState", 2);
			nbt.putInt("Cooking", 400);
		});
		scene.world().modifyBlock(stationPos, s -> s
				.setValue(CookingStationBlock.HAS_DONER, true)
				.setValue(CookingStationBlock.COOKED, false), false);
		scene.idle(60);

		scene.overlay().showText(90)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.cooking_station.text_3")
				.pointAt(util.vector().centerOf(stationPos))
				.placeNearTarget();
		scene.idle(30);
		scene.overlay().showControls(util.vector().topOf(stationPos), Pointing.DOWN, 40)
				.withItem(AllItems.GOGGLES.asStack());
		scene.idle(90);

		scene.world().modifyBlockEntityNBT(util.select().position(stationPos), BlockEntity.class, nbt -> {
			nbt.putInt("DonerState", 3);
			nbt.putInt("Cooking", 1200);
		});
		scene.world().modifyBlock(stationPos, s -> s.setValue(CookingStationBlock.COOKED, true), false);
		scene.idle(20);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.cooking_station.text_4")
				.pointAt(util.vector().centerOf(outF))
				.placeNearTarget();
		scene.idle(20);
		scene.world().createItemEntity(util.vector().centerOf(outF).add(0, 0, 0.4),
				util.vector().of(0, 0, 0.1), ModItems.COOKED_FULL_DONER.get().getDefaultInstance());
		scene.idle(70);

		scene.overlay().showText(80)
				.attachKeyFrame()
				.text("create_spinning_doners.ponder.cooking_station.text_5")
				.pointAt(util.vector().blockSurface(stationPos, Direction.WEST))
				.placeNearTarget();
		scene.idle(20);
		scene.overlay().showControls(util.vector().blockSurface(stationPos, Direction.WEST), Pointing.RIGHT, 40)
				.rightClick()
				.withItem(AllItems.WRENCH.asStack());
		scene.idle(20);
		scene.world().modifyBlock(stationPos, s -> s.setValue(CookingStationBlock.LEFT_SHAFT, false), false);
		scene.world().modifyBlockEntityNBT(util.select().position(shaftWest), BlockEntity.class, nbt -> nbt.putFloat("Speed", 0f));
		scene.idle(60);

		scene.overlay().showText(90)
				.text("create_spinning_doners.ponder.cooking_station.text_6")
				.pointAt(util.vector().blockSurface(stationPos, Direction.NORTH))
				.placeNearTarget();
		scene.idle(60);

		scene.markAsFinished();
	};
}
