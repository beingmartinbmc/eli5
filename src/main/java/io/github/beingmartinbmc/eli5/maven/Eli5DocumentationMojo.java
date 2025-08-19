package io.github.beingmartinbmc.eli5.maven;

import io.github.beingmartinbmc.eli5.cli.Eli5DocumentationGenerator;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Maven plugin for generating ELI5 documentation.
 * This plugin can be run independently of the build process.
 */
@Mojo(name = "generate-docs", defaultPhase = LifecyclePhase.NONE, requiresProject = true)
public class Eli5DocumentationMojo extends AbstractMojo {
    
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;
    
    @Parameter(property = "eli5.sourceDirectory", defaultValue = "${project.basedir}/src/main/java")
    private String sourceDirectory;
    
    @Parameter(property = "eli5.outputFile", defaultValue = "${project.build.directory}/eli5-docs/eli5.md")
    private String outputFile;
    
    @Parameter(property = "eli5.scanOnly", defaultValue = "false")
    private boolean scanOnly;
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("ELI5 Documentation Generator");
            getLog().info("========================");
            
            Eli5DocumentationGenerator generator = new Eli5DocumentationGenerator();
            
            if (scanOnly) {
                getLog().info("Scanning for @ExplainLikeImFive annotations...");
                generator.run(new String[]{"scan", sourceDirectory});
            } else {
                getLog().info("Generating ELI5 documentation...");
                getLog().info("Source directory: " + sourceDirectory);
                getLog().info("Output file: " + outputFile);
                
                generator.run(new String[]{"generate", sourceDirectory, outputFile});
            }
            
            getLog().info("ELI5 documentation generation completed successfully!");
            
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to generate ELI5 documentation", e);
        }
    }
}
