package co.neeve.nae2.common.registration.registry.helpers;


import appeng.api.parts.IPartModel;
import appeng.core.AELog;
import appeng.items.parts.PartModels;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Helps with the reflection magic needed to gather all models for AE2 cable bus parts.
 */
public class PartModelsHelper {

	public static List<ResourceLocation> createModels(Class<?> clazz) {
		List<ResourceLocation> locations = new ArrayList<>();

		// Check all static fields for used models
		var fields = clazz.getDeclaredFields();
		for (var field : fields) {
			if (field.getAnnotation(PartModels.class) == null) {
				continue;
			}

			if (!Modifier.isStatic(field.getModifiers())) {
				AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: " + field);
				continue;
			}

			Object value;
			try {
				field.setAccessible(true);
				value = field.get(null);
			} catch (IllegalAccessException e) {
				AELog.error(e, "Cannot access field annotated with @PartModels: " + field);
				continue;
			}

			convertAndAddLocation(field, value, locations);
		}

		// Check all static methods for the annotation
		for (var method : clazz.getDeclaredMethods()) {
			if (method.getAnnotation(PartModels.class) == null) {
				continue;
			}

			if (!Modifier.isStatic(method.getModifiers())) {
				AELog.error("The @PartModels annotation can only be used on static fields or methods. Was seen on: " + method);
				continue;
			}

			// Check for parameter count
			if (method.getParameters().length != 0) {
				AELog.error(
					"The @PartModels annotation can only be used on static methods without parameters. Was seen on: " + method);
				continue;
			}

			// Make sure we can handle the return type
			var returnType = method.getReturnType();
			if (!ResourceLocation.class.isAssignableFrom(returnType) && !Collection.class.isAssignableFrom(returnType)) {
				AELog.error(
					"The @PartModels annotation can only be used on static methods that return a ResourceLocation or" +
						" " +
						"Collection of " + "ResourceLocations. Was seen on: " + method);
				continue;
			}

			Object value;
			try {
				method.setAccessible(true);
				value = method.invoke(null);
			} catch (IllegalAccessException | InvocationTargetException e) {
				AELog.error(e, "Failed to invoke the @PartModels annotated method " + method);
				continue;
			}

			convertAndAddLocation(method, value, locations);
		}

		if (clazz.getSuperclass() != null) {
			locations.addAll(createModels(clazz.getSuperclass()));
		}

		return locations;
	}

	private static void convertAndAddLocation(Object source, Object value, List<ResourceLocation> locations) {
		if (value == null) {
			return;
		}

		if (value instanceof ResourceLocation) {
			locations.add((ResourceLocation) value);
		} else if (value instanceof IPartModel) {
			locations.addAll(((IPartModel) value).getModels());
		} else if (value instanceof Collection<?> values) {
			// Check that each object is an IPartModel
			for (var candidate : values) {
				if (!(candidate instanceof IPartModel)) {
					AELog.error("List of locations obtained from {} contains a non resource location: {}",
						source,
						candidate);
					continue;
				}

				locations.addAll(((IPartModel) candidate).getModels());
			}
		}
	}

}