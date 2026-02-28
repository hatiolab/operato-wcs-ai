package xyz.elidom.sys.system.print;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.attribute.standard.PrinterResolution;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.util.ValueUtil;

/**
 * 서버에서 직접 프린터를 연결하여 출력하는 서비스
 * 
 * @author shortstop
 */
@Component
public class ElidomPrintingService {
	/**
	 * 프린팅 관련 설정
	 */
	@Autowired
	private ElidomPrintingConfig printConfig;
	
	/**
	 * 프린터 명으로 프린트 서비스 찾아 리턴
	 * 
	 * @param printerName
	 * @return
	 */
	public PrintService getPrintService(String printerName) {
		printerName = ValueUtil.isEmpty(printerName) ? this.printConfig.getDefaultPrinterName() : printerName;
		PrintService printService = null;
		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

		for (PrintService service : services) {
			if (service.getName().equals(printerName)) {
				printService = service;
				break;
			}
		}

		if (printService == null) {
			throw new ElidomRuntimeException("프린트 명으로 프린트 찾기에 실패했습니다.");
		}

		return printService;
	}
	
	/**
	 * 바코드 라벨 인쇄 처리
	 * 
	 * @param printService
	 * @param command
	 * @param charSet
	 * @throws Exception
	 */
	public void printZplCode(PrintService printService, String command, String charSet) throws Exception {
		// 1. 캐릭터 셋 설정
		charSet = ValueUtil.isEmpty(charSet) ? this.printConfig.getDefaultCharSet() : charSet;
		
		// 2. 인쇄 내용 빌드
		byte[] input = command.getBytes(charSet);
		DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
		Doc doc = new SimpleDoc(input, flavor, new HashDocAttributeSet());

		// 3. 라벨 인쇄 처리
		DocPrintJob job = printService.createPrintJob();
		job.print(doc, null);
	}
	
	/**
	 * PDF를 Stream으로 인쇄
	 * 
	 * @param printService
	 * @param stream
	 * @param charSet
	 * @param dpi
	 */
	public void printPdfStream(PrintService printService, InputStream stream, String charSet, int dpi) {
		// 1. 캐릭터 셋 설정 체크
		charSet = ValueUtil.isEmpty(charSet) ? this.printConfig.getDefaultCharSet() : charSet;
		
		// 2. DPI 설정 체크 
		dpi = dpi <= 0 ? 1200 : dpi;
		
		// 3. PDF 인쇄 처리
		this.printPdfStream(printService, stream, 0, 0, dpi);
	}
	
	/**
	 * Stream 방식의 인쇄 처리
	 * 
	 * @param printService
	 * @param inputStream
	 * @param width
	 * @param height
	 * @param dpi
	 */
	public void printPdfStream(PrintService printService, InputStream inputStream, int width, int height, int dpi) {
		// 1. DPI 설정 체크 
		dpi = dpi <= 0 ? 1200 : dpi;
		
		// 2. 인쇄 처리
		PDDocument pdDoc = null;
		try {
			PrinterResolution printerResolution = new PrinterResolution(dpi, dpi, PrinterResolution.DPI);			
			DocAttributeSet das = new HashDocAttributeSet();
			das.add(printerResolution);
			pdDoc = PDDocument.load(inputStream);
			PDPageTree list = pdDoc.getDocumentCatalog().getPages();
			PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
			pras.add(new Copies(1));
			pras.add(printerResolution);
			
			for (PDPage page : list) {
				DocPrintJob job = printService.createPrintJob();
				PDDocument newDoc = new PDDocument();
				newDoc.addPage(page);
				
		        PDFRenderer pdfRenderer = new PDFRenderer(newDoc);
		        BufferedImage image = pdfRenderer.renderImage(0, 2.775f, ImageType.RGB);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(image, "png", baos);
				Doc doc = new SimpleDoc(baos.toByteArray(), DocFlavor.BYTE_ARRAY.PNG, das);
				job.print(doc, pras);
				newDoc.close();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);

		} finally {
			if (pdDoc != null) {
				try {
					pdDoc.close();
				} catch (IOException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		}
	}
}
