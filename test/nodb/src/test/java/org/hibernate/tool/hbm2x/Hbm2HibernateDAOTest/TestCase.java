/*
 * Created on 2004-12-01
 *
 */
package org.hibernate.tool.hbm2x.Hbm2HibernateDAOTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Persistence;

import org.apache.commons.logging.Log;
import org.hibernate.Version;
import org.hibernate.tool.api.metadata.MetadataDescriptor;
import org.hibernate.tool.internal.export.dao.DAOExporter;
import org.hibernate.tool.internal.export.pojo.POJOExporter;
import org.hibernate.tools.test.util.FileUtil;
import org.hibernate.tools.test.util.HibernateUtil;
import org.hibernate.tools.test.util.JUnitUtil;
import org.hibernate.tools.test.util.JavaUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * @author max
 * @author koen
 */
public class TestCase {

	private static final String[] HBM_XML_FILES = new String[] {
			"Article.hbm.xml",
			"Author.hbm.xml"				
	};
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	private File outputDir = null;
	private File resourcesDir = null;

	@Before
	public void setUp() throws Exception {
		outputDir = new File(temporaryFolder.getRoot(), "output");
		outputDir.mkdir();
		resourcesDir = new File(temporaryFolder.getRoot(), "resources");
		resourcesDir.mkdir();
		MetadataDescriptor metadataDescriptor = HibernateUtil
				.initializeMetadataDescriptor(this, HBM_XML_FILES, resourcesDir);
		POJOExporter javaExporter = new POJOExporter();
		javaExporter.setMetadataDescriptor(metadataDescriptor);
		javaExporter.setOutputDirectory(outputDir);
		POJOExporter exporter = new DAOExporter();
		exporter.setMetadataDescriptor(metadataDescriptor);
		exporter.setOutputDirectory(outputDir);
		exporter.getProperties().setProperty("ejb3", "false");
		exporter.getProperties().setProperty("jdk5", "true");
		exporter.start();
		javaExporter.start();
	}
	
	@Test
	public void testFileExistence() {
		JUnitUtil.assertIsNonEmptyFile(new File(
				outputDir, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/ArticleHome.java") );
		JUnitUtil.assertIsNonEmptyFile(new File(
				outputDir, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/AuthorHome.java") );
	}
	
	@Test
	public void testCompilable() throws IOException {
		File compiled = new File(temporaryFolder.getRoot(), "compiled");
		compiled.mkdir();
		FileUtil.generateNoopComparator(outputDir);
		List<String> jars = new ArrayList<String>();
		jars.add(JavaUtil.resolvePathToJarFileFor(Log.class)); // for commons logging
		jars.add(JavaUtil.resolvePathToJarFileFor(Persistence.class)); // for jpa api
		jars.add(JavaUtil.resolvePathToJarFileFor(Version.class)); // for hibernate core
		JavaUtil.compile(outputDir, compiled, jars);
		JUnitUtil.assertIsNonEmptyFile(new File(
				compiled, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/AuthorHome.class") );
		JUnitUtil.assertIsNonEmptyFile(new File(
				compiled, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/Author.class") );
		JUnitUtil.assertIsNonEmptyFile(new File(
				compiled, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/ArticleHome.class") );
		JUnitUtil.assertIsNonEmptyFile(new File(
				compiled, 
				"org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/Article.class") );
		JUnitUtil.assertIsNonEmptyFile(new File(
				compiled, 
				"comparator/NoopComparator.class") );
	}
    
	@Test
	public void testNoVelocityLeftOvers() {
		Assert.assertNull(FileUtil.findFirstString(
			"$",
			new File(outputDir, "org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/ArticleHome.java")));
        Assert.assertNull(FileUtil.findFirstString(
        		"$",
        		new File(outputDir, "org/hibernate/tool/hbm2x/Hbm2HibernateDAOTest/AuthorHome.java") ) );       
	}

}
