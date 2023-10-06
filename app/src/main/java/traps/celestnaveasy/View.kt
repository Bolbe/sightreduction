package traps.celestnaveasy

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.format.DateTimeFormatter
import kotlin.math.abs

fun isInRange(str: String, min: Double, max: Double): Boolean {
    if (str.isEmpty()) return true
    try {
        val d = str.toDouble()
        if (d>=min && d<max) return true
        return true
    } catch (e: Exception) {}
    return false
}

fun isInRange(str: String, min: Int, max: Int): Boolean {
    if (str.isEmpty()) return true
    try {
        val i = str.toInt()
        if (i in min..max) return true
    } catch (e: Exception) {}
    return false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPanel() {
    val focusManager = LocalFocusManager.current
    Column (
        modifier = Modifier.verticalScroll(rememberScrollState())
    ){

        Text(
            "FEUILLE DE CALCUL\nDROITE DE HAUTEUR\nLINE OF POSITION",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
        Text(
            "Cette feuille de calcul permet de tracer une droite de hauteur (line of position) après une mesure au sextant de la hauteur du soleil. "+
            "Elle intègre un algorithme de calcul des positions du soleil (https://midcdmz.nrel.gov/spa) pour une date+heure donnée, ce qui permet de sauter (ou vérifier) l'étape de consultation des almanachs papier. "+
            "Elle déroule ensuite les calculs pas à pas de manière pédagogique. Elle donne au final la longueur et la direction de l'intercept qui vous permettra de tracer sur la carte votre droite de hauteur.",
            modifier = Modifier.fillMaxSize()
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("Date du jour", fontSize = 25.sp)
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())
        Row(
            modifier = Modifier.fillMaxWidth(),
            ) {

            TextField(
                value = mainController.dayStr.value,
                label = { Text("Jour") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 1, 31)) {
                        mainController.dayStr.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
            TextField(
                value = mainController.monthStr.value,
                label = { Text("Mois") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 1, 12)) {
                        mainController.monthStr.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
            TextField(
                value = mainController.yearStr.value,
                label = { Text("Année") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0, 3000)) {
                        mainController.yearStr.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Point de calcul G", fontSize = 25.sp)
        Text("Le point de calcul est un point d'observation arbitraire, proche de votre position réelle. On prend généralement la position estimée depuis le dernier point sextant. C'est par rapport à ce point que l'on se positionnera sur la carte.")
        Text("Latitude (LatG)", fontSize = 20.sp)
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())
        DMSComponent(mainController.latitudeDMS, "N", "S")
        Text("Longitude (LonG)", fontSize = 20.sp)
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())
        DMSComponent(mainController.longitudeDMS, "E", "W")

        Spacer(modifier = Modifier.height(20.dp))
        Text("Correction instrument", fontSize = 25.sp)
        Text("Il s'agit de l'erreur intrasèque de votre sextant. On l'obtient en alignant l'horizon direct avec l'horizon reflété. Si l'alignement est obtenue en remontant le reflet, la correction est positive, sinon elle est negative.")
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())
        TextField(
            value = mainController.correctionIndexStr.value,
            label = { Text("Minutes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            onValueChange = { newText ->
                if (isInRange(newText, -100.0, 100.0)) {
                    mainController.correctionIndexStr.value = newText
                    mainController.dataReady.value = false
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Hauteur oeil", fontSize = 25.sp)
        Text("Hauteur à laquelle se trouve votre oeil au dessus de l'eau.")
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())
        TextField(
            value = mainController.eyeHeightStr.value,
            label = { Text("Mètres") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            onValueChange = { newText ->
                if (isInRange(newText,0.0, 60.0)) {
                    mainController.eyeHeightStr.value = newText
                    mainController.dataReady.value = false
                }
            }
        )

        Spacer(modifier = Modifier.height(25.dp))
        Row (
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround
        ){
            RadioButtonText("1 mesure", !mainController.fiveMeasures.value) {
                mainController.fiveMeasures.value = false
                mainController.dataReady.value = false
            }
            RadioButtonText("5 mesures", mainController.fiveMeasures.value) {
                mainController.fiveMeasures.value = true
                mainController.dataReady.value = false
            }
        }
        if (mainController.fiveMeasures.value)
            Text("Quand on débute dans la manipulation du sextant, on peut améliorer la précision en faisant 5 mesures successives à moins d'une minute d'intervalle. Cette feuille de calcul choisira la meilleure mesure parmi les 5 en procédant à des régressions linéaires. La mesure choisie sera la plus proche de la droite passant au plus proche des 3 points les plus alignés.")

        val measureCount = if (mainController.fiveMeasures.value) 5 else 1
        for (i in 0 until measureCount)
            PointEditor(mainController.measureList[i])

        Spacer(modifier = Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                mainController.compute()
            }
            ) {
                Text(text = "Montrer les calculs")
            }
        }

        if (mainController.error.value.isNotEmpty()) {
            Text(
                text = mainController.error.value,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onError
            )
        }

        if (mainController.dataReady.value && mainController.selectedMeasure!=null) {
            val rawMeasure = mainController.selectedMeasure!!
            Spacer(modifier = Modifier.height(20.dp))

            //if (mainController.fiveMeasures.value) Text(mainController.explain)
            ResultRow("Heure Mesure retenue", "${rawMeasure.timeValue.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}", MaterialTheme.colorScheme.primaryContainer)
            Text("Le ${mainController.day}-${mainController.month}-${mainController.year} à ${rawMeasure.timeValue.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}, " +
                "les almanachs papiers ou les algorithmes calculant la position du soleil nous donnent les valeurs suivantes:")

            ResultRow("Sun declination (DEC)\n(Latitude du soleil)\nPositif au nord", "${"%.${5}f°".format(mainController.sunDeclination)}")
            ResultRow("Greenwich Hour Angle (GHA)\n(Longitude du soleil)\nPositif à l'est", "${"%.${5}f°".format(mainController.sunHourAngle)}")

            Text("L'angle entre la longitude du point de calcul LatG et du soleil GHA est donc:")
            ResultRow("Local Hour Angle LHA", "${"%.${5}f°".format(mainController.localHourAngle)}")

            Text("Le principe est maintenant de calculer la hauteur et l'azimuth du soleil si on l'observait depuis le point de calcul.")
            Text("Hauteur calculée Hc:")
            Image(
                painterResource(id = R.mipmap.hc),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().height(50.dp).horizontalScroll(rememberScrollState()),
                contentScale = ContentScale.FillHeight
            )
            Text("Avec les valeurs suivantes:")

            ResultRow("DEC", "${"%.${5}f°".format(mainController.sunDeclination)}")
            ResultRow("LatG", "${"%.${5}f°".format(mainController.latG)}")
            ResultRow("LHA", "${"%.${5}f°".format(mainController.localHourAngle)}")
            Text("On obtient:")
            ResultRow("Hc (décimale)", "${"%.${5}f°".format(mainController.elevation)}", MaterialTheme.colorScheme.primaryContainer)
            ResultRow("c'est à dire", "${Calc.decimalToSexaStr(mainController.elevation)}", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            Text("Azimuth:")
            Image(
                painterResource(id = R.mipmap.azimuth),
                contentDescription = null,
                modifier = Modifier.fillMaxSize().height(100.dp).horizontalScroll(rememberScrollState()),
                contentScale = ContentScale.FillHeight
            )
            Text("\nAzimuth = z si matin\nAzimuth = 360-z si après-midi\n", Modifier.background(MaterialTheme.colorScheme.secondaryContainer).fillMaxSize())
            Text("Avec les valeurs suivantes:")
            ResultRow("DEC", "${"%.${5}f°".format(mainController.sunDeclination)}")
            ResultRow("LatG", "${"%.${5}f°".format(mainController.latG)}")
            ResultRow("Hc", "${"%.${5}f°".format(mainController.elevation)}")
            Text("On obtient:")
            ResultRow("Azimuth", "${mainController.azimuth}°", MaterialTheme.colorScheme.primaryContainer)
            Text("\nRevenons à notre mesure retenue:")
            ResultRow("Mesure decimale retenue", "${"%.${3}f°".format(mainController.rawMeasure)}", MaterialTheme.colorScheme.primaryContainer)
            Text("On applique les corrections (minutes):")
            ResultRow("Instrument", "${mainController.correctionIndexStr.value}'")
            ResultRow("Hauteur oeil", "${"%.${5}f".format(mainController.correctionDip)}'")
            ResultRow("Demi diametre soleil", "${mainController.correctionDiameter}'")
            ResultRow("Refraction", "${"%.${5}f".format(mainController.correctionRefraction)}'")
            ResultRow("Mesure corrigée", "${"%.${3}f°".format(mainController.correctedMeasure)}", MaterialTheme.colorScheme.secondaryContainer)
            ResultRow("c'est à dire", "${Calc.decimalToSexaStr(mainController.correctedMeasure)}", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)

            Text("Depuis le point de calcul, la hauteur du soleil serait")
            ResultRow("", "${Calc.decimalToSexaStr(mainController.elevation)}")
            Text("Or depuis notre position réelle, on l'a mesurée à")
            ResultRow("", "${Calc.decimalToSexaStr(mainController.correctedMeasure)}")
            Text("L'intercept mesure donc")
            ResultRow("Intercept", "${"%.${1}f".format(mainController.intercept)} NM", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            if (mainController.towards_sun) {
                Text("La hauteur mesurée est supérieure à la hauteur vue depuis le point de calcul, donc l'intercept part du point de calcul et VA VERS le soleil.")
                ResultRow("Direction intercept", "${mainController.azimuth}°", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            }
            else {
                Text("La hauteur mesurée est inférieure à la hauteur vue depuis le point de calcul, donc l'intercept part du point de calcul et S'ELOIGNE du soleil.")
                ResultRow("Direction intercept", "${mainController.azimuth180}°", MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DMSComponent(dms: DMS, positiveText: String, negativeText:String) {
    val focusManager = LocalFocusManager.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField (
            value = dms.degStr.value,
            singleLine = true,
            label = { Text("Degrés") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
            onValueChange = { newText ->
                if (isInRange(newText, 0, 90)) {
                    mainController.dataReady.value = false
                    dms.degStr.value = newText
                }
            }
        )
        TextField (
            value = dms.minStr.value,
            singleLine = true,
            label = { Text("Minutes") },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            onValueChange = { newText ->
                if (isInRange(newText, 0.0, 60.0)) {
                    mainController.dataReady.value = false
                    dms.minStr.value = newText
                }
            }
        )
        val buttonText = if (dms.positive.value) positiveText else negativeText
        Button(onClick = {
            dms.positive.value = !dms.positive.value
            mainController.dataReady.value = false
        }
        ) {
            Text(text = "   ${buttonText}   ")
        }
    }
}

@Composable
fun RadioButtonText(text: String, selected: Boolean, onClick: () -> Unit) {
    Row (
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.selectable(
            selected = selected,
            onClick = onClick
        )
    ){
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ResultRow(
    title: String,
    value: String,
    bgColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onBackground)
{

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 16.sp, color = textColor)
        Text(value, fontSize = 20.sp, color = textColor)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PointEditor(measure: Measure) {
    val focusManager = LocalFocusManager.current

    Column {
        Spacer(modifier = Modifier.height(25.dp))
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(measure.title, fontSize = 25.sp)
            Button(onClick = {
                measure.clear()
            }
            ) {
                Text(text = "Effacer")
            }
        }
        Spacer(modifier = Modifier
            .height(3.dp)
            .background(color = MaterialTheme.colorScheme.primary)
            .fillMaxWidth())

        Row(Modifier.fillMaxWidth()) {
            TextField(
                value = measure.timeHour.value,
                modifier = Modifier.weight(1f),
                label = { Text("Heure") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0, 24)) {
                        measure.timeHour.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
            TextField(
                value = measure.timeMinute.value,
                modifier = Modifier.weight(1f),
                label = { Text("Minute") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0, 60)) {
                        measure.timeMinute.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
            TextField(
                value = measure.timeSecond.value,
                modifier = Modifier.weight(1f),
                label = { Text("Second") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0, 60)) {
                        measure.timeSecond.value = newText
                        mainController.dataReady.value = false
                    }
                }
            )
        }
        Row(Modifier.fillMaxWidth()) {
            TextField (
                value = measure.deg.value,
                label = { Text("Degrés") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0, 90)) {
                        mainController.dataReady.value = false
                        measure.deg.value = newText
                    }
                }
            )
            TextField (
                value = measure.min.value,
                label = { Text("Minutes") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                onValueChange = { newText ->
                    if (isInRange(newText, 0.0, 60.0)) {
                        mainController.dataReady.value = false
                        measure.min.value = newText
                    }
                }
            )
        }
    }
}
