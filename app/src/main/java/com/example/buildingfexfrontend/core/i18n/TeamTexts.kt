package com.example.buildingfexfrontend.core.i18n

/**
 * ES/EN texts for the team screen (`team.*`).
 * Feature-owned by team/presentation (TeamScreen, TeamViewModel).
 */
val TeamTexts: Map<String, TextPair> = mapOf(
    "team.fab.add" to ("Agregar miembro" to "Add member"),
    "team.title" to ("Equipo" to "Team"),
    "team.subtitle" to ("Personal y proveedores del condominio." to "Condominium staff and suppliers."),
    "team.search.placeholder" to ("Buscar por nombre, DNI o teléfono" to "Search by name, ID or phone"),
    "team.search.clear" to ("Limpiar búsqueda" to "Clear search"),
    "team.empty.none" to ("Aún no hay miembros en el equipo." to "There are no team members yet."),
    "team.empty.noMatch" to ("Sin resultados para \"{query}\"." to "No results for \"{query}\"."),
    "team.editor.titleNew" to ("Nuevo miembro del equipo" to "New team member"),
    "team.editor.titleEdit" to ("Editar miembro" to "Edit member"),
    "team.field.name" to ("Nombre" to "Name"),
    "team.field.phone" to ("Teléfono" to "Phone"),
    "team.field.dni" to ("DNI" to "ID"),
    "team.field.salary" to ("Salario" to "Salary"),
    "team.field.photo" to ("Foto" to "Photo"),
    "team.editor.saving" to ("Guardando…" to "Saving…"),
    "team.editor.save" to ("Guardar" to "Save"),
    "team.editor.cancel" to ("Cancelar" to "Cancel"),
    "team.delete.title" to ("Eliminar miembro" to "Delete member"),
    "team.delete.message" to ("¿Eliminar a {name} del equipo? Esta acción no se puede deshacer." to "Remove {name} from the team? This action cannot be undone."),
    "team.delete.confirm" to ("Eliminar" to "Delete"),
    "team.message.title" to ("Listo" to "Done"),
    "team.message.confirm" to ("Entendido" to "OK"),
    "team.card.dniPhone" to ("DNI {dni} · {phone}" to "ID {dni} · {phone}"),
    "team.card.salary" to ("Salario: {salary}" to "Salary: {salary}"),
    "team.card.editCd" to ("Editar {name}" to "Edit {name}"),
    "team.card.deleteCd" to ("Eliminar {name}" to "Delete {name}"),
    "team.msg.photoFailed" to ("No se pudo procesar la imagen. Intenta con otra." to "The image could not be processed. Try another one."),
    "team.msg.removed" to ("Miembro eliminado del equipo." to "Member removed from the team."),
    "team.msg.added" to ("Miembro agregado al equipo." to "Member added to the team."),
    "team.msg.updated" to ("Miembro actualizado." to "Member updated."),
)
