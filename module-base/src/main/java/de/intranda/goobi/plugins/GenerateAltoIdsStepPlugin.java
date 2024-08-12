package de.intranda.goobi.plugins;

/**
 * This file is part of a plugin for Goobi - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.StorageProviderInterface;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.goobi.beans.Step;
import org.goobi.io.BackupFileManager;
import org.goobi.production.enums.LogType;
import org.goobi.production.enums.PluginGuiType;
import org.goobi.production.enums.PluginReturnValue;
import org.goobi.production.enums.PluginType;
import org.goobi.production.enums.StepReturnValue;
import org.goobi.production.plugin.interfaces.IStepPluginVersion2;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@PluginImplementation
@Log4j2
public class GenerateAltoIdsStepPlugin implements IStepPluginVersion2 {

    @Getter
    private String title = "intranda_step_generate_alto_ids";
    @Getter
    private Step step;

    @Getter
    private String textBlockIdPrefix;
    @Getter
    private String textLineIdPrefix;
    @Getter
    private String textWordIdPrefix;

    private XPathExpression<Element> blockExpression;
    private XPathExpression<Element> lineExpression;
    private XPathExpression<Element> wordExpression;

    private String returnPath;

    @Override
    public void initialize(Step step, String returnPath) {
        this.returnPath = returnPath;
        this.step = step;

        textBlockIdPrefix = "TextBlock_";
        textLineIdPrefix = "TextLine_";
        textWordIdPrefix = "TextWord_";

        XPathFactory xPathFactory = XPathFactory.instance();
        blockExpression = xPathFactory.compile("//*[local-name()='TextBlock']", Filters.element());
        lineExpression = xPathFactory.compile("//*[local-name()='TextBlock']/*[local-name()='TextLine']", Filters.element());
        wordExpression = xPathFactory.compile("//*[local-name()='TextLine']/*[local-name()='String']", Filters.element());

        log.info("GenerateAltoIds step plugin initialized");
    }

    @Override
    public PluginGuiType getPluginGuiType() {
        return PluginGuiType.NONE;
    }

    @Override
    public String getPagePath() {
        return "/uii";
    }

    @Override
    public PluginType getType() {
        return PluginType.Step;
    }

    @Override
    public String cancel() {
        return "/uii" + returnPath;
    }

    @Override
    public String finish() {
        return "/uii" + returnPath;
    }

    @Override
    public int getInterfaceVersion() {
        return 0;
    }

    @Override
    public HashMap<String, StepReturnValue> validate() {
        return null;
    }

    @Override
    public boolean execute() {
        PluginReturnValue ret = run();
        return ret != PluginReturnValue.ERROR;
    }

    @Override
    public PluginReturnValue run() {
        boolean successful = true;

        try {
            StorageProviderInterface storage = StorageProvider.getInstance();
            Path altoFolder = Path.of(getStep().getProzess().getOcrAltoDirectory());

            List<Path> altoFiles = storage.listFiles(altoFolder.toString());

            if (anyAltoFileHasMissingIds(altoFiles)) {
                createBackupOfAltoFiles();
                generateMissingAltoIds(altoFiles);
            }
        } catch (SwapException | IOException | JDOMException e) {
            Helper.addMessageToProcessJournal(getStep().getProcessId(), LogType.ERROR, e.getMessage());
            successful = false;
        }

        log.info("GenerateAltoIds step plugin executed");
        if (!successful) {
            return PluginReturnValue.ERROR;
        }
        return PluginReturnValue.FINISH;
    }

    private void createBackupOfAltoFiles() throws IOException, SwapException {
        Path ocrDirectory = Path.of(getStep().getProzess().getOcrDirectory());
        BackupFileManager.createBackup(ocrDirectory.getParent().toString(), ocrDirectory.getFileName().toString(), true);
    }

    private void saveAltoFile(Path altoFile, Document document) throws IOException {
        // Create a FileWriter to overwrite the XML file
        FileWriter writer = new FileWriter(altoFile.toString());

        // Output the updated XML document to the file
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        xmlOutputter.output(document, writer);

        // Close the FileWriter
        writer.close();
    }

    protected boolean anyAltoFileHasMissingIds(List<Path> altoFiles) throws IOException, JDOMException {
        for (Path altoFile : altoFiles) {
            if (altoFileHasMissingIds(altoFile)) {
                return true;
            }
        }
        return false;
    }

    protected boolean altoFileHasMissingIds(Path altoFile) throws IOException, JDOMException {
        Element root = getXmlRootElementOfFile(altoFile);

        if (blockExpression.evaluate(root).stream()
                .anyMatch(this::hasNoId)) {
            return true;
        }
        if (lineExpression.evaluate(root).stream()
                .anyMatch(this::hasNoId)) {
            return true;
        }
        if (wordExpression.evaluate(root).stream()
                .anyMatch(this::hasNoId)) {
            return true;
        }

        return false;
    }

    private boolean hasNoId(Element e) {
        return e.getAttribute("ID") == null;
    }

    private Element getXmlRootElementOfFile(Path altoFile) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(altoFile.toString());
        return document.getRootElement();
    }

    private void generateMissingAltoIds(List<Path> altoFiles) throws IOException, JDOMException {
        for (Path altoFile : altoFiles) {
            generateMissingAltoIds(altoFile);
        }
    }

    private void generateMissingAltoIds(Path altoFile) throws IOException, JDOMException {
        Element root = getXmlRootElementOfFile(altoFile);

        generateIdsForElementType(blockExpression.evaluate(root), new IdGenerator(textBlockIdPrefix));
        generateIdsForElementType(lineExpression.evaluate(root), new IdGenerator(textLineIdPrefix));
        generateIdsForElementType(wordExpression.evaluate(root), new IdGenerator(textWordIdPrefix));

        saveAltoFile(altoFile, root.getDocument());
    }

    @RequiredArgsConstructor
    static
    class IdGenerator {
        @NonNull
        private String prefix;
        private long counter = 1L;

        public String generate() {
            return prefix + counter++;
        }
    }

    private void generateIdsForElementType(List<Element> elements, IdGenerator idGenerator) {
        Set<String> usedIds = elements.stream()
                .map(e -> e.getAttribute("ID"))
                .filter(Objects::nonNull)
                .map(Attribute::getValue)
                .collect(Collectors.toSet());
        // Add ID for all elements that have none
        for (Element e : elements) {
            if (hasNoId(e)) {
                String newId;
                do {
                    newId = idGenerator.generate();
                } while (usedIds.contains(newId));
                usedIds.add(newId);
                e.setAttribute("ID", newId);
            }
        }
    }
}
