package de.cronn.diff;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.rules.TestName;

import de.cronn.diff.html.HtmlBuilder;
import de.cronn.diff.util.FileHelper;


public class TestBase {

	private static final String TEST_DATA_VALIDATION_DIR = "data/test/validation/";
	private static final String TEST_DATA_VALIDATION_DIR_WIN = FilenameUtils.separatorsToWindows(TEST_DATA_VALIDATION_DIR);

	public static final String TEST_DATA_INPUT_DIR = "data/test/input/";
	private static final String TEST_DATA_INPUT_DIR_WIN = FilenameUtils.separatorsToWindows(TEST_DATA_INPUT_DIR);

	static final String TEST_DATA_OUTPUT_DIR = "data/test/output/";
	private static final String TEST_DATA_OUTPUT_DIR_WIN = FilenameUtils.separatorsToWindows(TEST_DATA_OUTPUT_DIR);
	
	private static final String HTML_SUFFIX = ".html";

	private static final String SYS_OUT_SUFFIX = ".sysouterr";

	private static boolean outputDirChecked = false;

	@Rule
	public TestName testName = new TestName();

	@Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

	@BeforeClass
	public static void assureTestOutputDir() throws IOException {
		createOutputDirIfNotExists();
	}
	
	@Before
	public void setUp() {
		HtmlBuilder.setUseSimpleFormatOnHtmls(true);
	}

	@After
	public void tearDown() {
		HtmlBuilder.setUseSimpleFormatOnHtmls(false);
	}

	protected void assertStringResultEqualToValidation(String actual) throws IOException {
		actual = FileHelper.normalizeLineSeparators(normalizeWorkingDir(actual));
		writeToDisk(actual, getOutFilePath()); // for external comparison by developer
		String expected = readFileToString(getValidationFilePath());
		expected = FileHelper.normalizeLineSeparators(expected);
		assertThat(actual).isEqualTo(expected);
	}

	void assertOutputEqualToValidation() throws IOException {
		String expected = readFileToString(getValidationHtmlFilePath());
		expected = FileHelper.normalizeLineSeparators(expected);
		String actual = readFileToString(getOutHtmlFilePath());
		actual = FileHelper.normalizeLineSeparators(normalizeTimestamps(actual));
		writeToDisk(actual, getOutHtmlFilePath());
		assertThat(actual).isEqualTo(expected);
	}

	void assertSysOutErrEqualToValidation(String actual) throws IOException {
		actual = normalizeWorkingDir(normalizeTestDataDirs(actual));
		writeToDisk(actual, getOutSysOutFilePath()); // for external comparison by developer
		String expected = readFileToString(getValidationSysOutFilePath());

		String expectedNorm = FileHelper.normalizeLineSeparators(expected);
		String actualNorm = FileHelper.normalizeLineSeparators(actual);
		assertThat(actualNorm).isEqualTo(expectedNorm);
	}

	String getOutHtmlFilePath() {
		return TEST_DATA_OUTPUT_DIR + getTestMethodName() + HTML_SUFFIX;
	}

	private String getTestMethodName() {
		return testName.getMethodName();
	}
	
	private String normalizeTimestamps(String s) {
		s = s.replaceAll("([0-9]{4}-[0-9]{2}-[0-9]{2})(.*?)([0-9]{2}:[0-9]{2}:[0-9]{2}[.]*[0-9]*)", "[DATE]$2[TIME]");
		s = s.replaceAll("(\\[TIME] )(\\+[0-9]{4})", "$1 [ZONE]");
		return s;
	}

	private String normalizeTestDataDirs(String s) {
		s = StringUtils.replace(s, TEST_DATA_INPUT_DIR_WIN, TEST_DATA_INPUT_DIR);
		s = StringUtils.replace(s, TEST_DATA_OUTPUT_DIR_WIN, TEST_DATA_OUTPUT_DIR);
		s = StringUtils.replace(s, TEST_DATA_VALIDATION_DIR_WIN, TEST_DATA_VALIDATION_DIR);
		return s;
	}

	private String normalizeWorkingDir(String s) {
		String workingDir = FileHelper.getWorkingDir();
		return StringUtils.replace(s, workingDir, "[current/working/directory/]");
	}

	private String readFileToString(String filePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filePath)));
	}

	private static void createOutputDirIfNotExists() throws IOException {
		if(!outputDirChecked) {
			Path outputDirPath = Paths.get(TEST_DATA_OUTPUT_DIR);
			if(Files.notExists(outputDirPath)){
				Files.createDirectory(outputDirPath);
			}
			outputDirChecked = true;
		}
	}

	private void writeToDisk(String str, String destPath) throws IOException {
		Files.write(Paths.get(destPath), str.getBytes());
	}

	private String getValidationFilePath() {
		return TEST_DATA_VALIDATION_DIR + getTestMethodName();
	}

	private String getValidationHtmlFilePath() {
		return TEST_DATA_VALIDATION_DIR + getTestMethodName() + HTML_SUFFIX;
	}

	private String getOutFilePath() {
		return TEST_DATA_OUTPUT_DIR + getTestMethodName();
	}

	private String getOutSysOutFilePath() {
		return TEST_DATA_OUTPUT_DIR + getTestMethodName() + SYS_OUT_SUFFIX;
	}

	private String getValidationSysOutFilePath() {
		return TEST_DATA_VALIDATION_DIR + getTestMethodName() + SYS_OUT_SUFFIX;
	}
}
