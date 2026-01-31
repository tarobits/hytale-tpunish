package dev.tarobits.punishments.utils.args;

import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArgUtils {
	private static final Map<CustomArgumentType, String> argumentDefinitionMap = Map.ofEntries(
			Map.entry(CustomArgumentType.SHADOW, "-s"), Map.entry(CustomArgumentType.BROADCAST, "-b"));

	public static Map<CustomArgumentType, String> decodeArguments(
			@Nonnull String input,
			@Nonnull Integer numberOfParametersToExclude,
			@Nonnull List<CustomArgumentType> possibleTypes
	) {
		List<CustomArgument> argumentList = new ArrayList<>();
		String[] individual = input.split(" ");

		for (int i = numberOfParametersToExclude; i < individual.length; i++) {
			if (individual[i] == null) {
				continue;
			}
			HytaleLogger.forEnclosingClass()
					.atInfo()
					.log(individual[i]);
			argumentList.add(getArgument(individual[i], possibleTypes));
		}

		return condenseDefaultIntoSingular(argumentList);
	}

	private static Map<CustomArgumentType, String> condenseDefaultIntoSingular(@Nonnull List<CustomArgument> argumentList) {
		StringBuilder result = new StringBuilder();
		Map<CustomArgumentType, String> argumentMap = new Object2ObjectOpenHashMap<>();
		argumentList.forEach((t) -> {
			if (t.type == CustomArgumentType.DEFAULT) {
				result.append(t.content)
						.append(" ");
			} else {
				argumentMap.put(t.type, t.content);
			}
		});
		argumentMap.put(
				CustomArgumentType.DEFAULT, result.toString()
						.trim()
		);
		return argumentMap;
	}

	private static CustomArgument getArgument(
			@Nonnull String input,
			@Nonnull List<CustomArgumentType> possibleTypes
	) {
		CustomArgumentType determinedType = CustomArgumentType.DEFAULT;
		for (CustomArgumentType t : CustomArgumentType.values()) {
			if (t == CustomArgumentType.DEFAULT) {
				continue;
			}
			if (!possibleTypes.contains(t)) {
				continue;
			}
			if (!input.contains(argumentDefinitionMap.get(t))) {
				continue;
			}
			determinedType = t;
			break;
		}
		return new CustomArgument(input, determinedType);
	}

	private record CustomArgument(String content, CustomArgumentType type) {
	}
}
