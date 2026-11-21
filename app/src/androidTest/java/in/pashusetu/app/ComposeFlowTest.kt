package in.pashusetu.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.FileOutputStream
import android.graphics.Bitmap

class ComposeFlowTest {
 @get:Rule val rule=createAndroidComposeRule<ComposeActivity>()
 private fun waitText(text:String){rule.waitUntil(20000){rule.onAllNodesWithText(text,substring=true).fetchSemanticsNodes().isNotEmpty()}}
 private fun snap(name:String){rule.waitForIdle();val b=InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()?:return;FileOutputStream(File(InstrumentationRegistry.getInstrumentation().targetContext.filesDir,name+".png")).use{b.compress(Bitmap.CompressFormat.PNG,100,it)};b.recycle()}
 @Test fun fullJourney(){
  waitText("Your farm, today.");snap("01-home-empty")
  rule.onNodeWithText("Register animal",useUnmergedTree=false).performClick();waitText("Animal identity")
  rule.onNodeWithText("Animal name *").performTextInput("Gauri QA")
  rule.onNodeWithText("Breed").performTextInput("Gir")
  rule.onNodeWithText("Ear tag / ID").performTextInput("QA-001")
  snap("02-registration")
  rule.onNodeWithText("Continue").performClick();waitText("Body & health")
  rule.onNodeWithText("Continue").performClick();waitText("Owner & details")
  rule.onNodeWithText("Save animal").performClick();waitText("Gauri QA");waitText("Animal passport");snap("03-passport")
  rule.onNodeWithText("Milk & growth").performScrollTo().performClick();waitText("Milk (litres)")
  rule.onNodeWithText("Milk (litres)").performTextInput("8.5")
  rule.onNodeWithText("Body weight (kg)").performTextInput("350")
  rule.onNodeWithText("Save record").performClick();waitText("350 kg")
  rule.onNodeWithText("Growth").performClick();waitText("Weight history · kg");snap("04-growth")
  rule.activityRule.scenario.recreate();waitText("Gauri QA");waitText("350 kg")
  rule.onNodeWithContentDescription("Back").performClick();waitText("Your farm, today.")
  rule.onNodeWithText("Reports",substring=false).performClick();waitText("Farm reports");snap("05-reports")
 }
 @Test fun backupValidation(){
  val a=animalFrom(mapOf("id" to "a","name" to "Backup cow","species" to "Cow","status" to "Healthy"))
  val c=careFrom(mapOf("id" to "c","animal" to "a","type" to "production","date" to today(),"milk" to "8"))
  val s=Register(listOf(a),listOf(c),true)
  val restored=parseBackup(s.backup());assertEquals("Backup cow",restored.animals.single().name);assertEquals("8",restored.care.single().fields()["milk"]);assertTrue(restored.hindi)
  assertTrue(runCatching { parseBackup(Register(emptyList(),listOf(c)).backup()) }.isFailure)
  assertTrue(runCatching { animalFrom(mapOf("name" to "A","species" to "Cow","weight" to "-3")) }.isFailure)
 }
}
