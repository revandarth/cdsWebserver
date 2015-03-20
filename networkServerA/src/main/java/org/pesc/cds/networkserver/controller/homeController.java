package org.pesc.cds.networkserver.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pesc.cds.networkserver.domain.Transaction;
import org.pesc.cds.networkserver.domain.TransactionsDao;
import org.pesc.cds.networkserver.service.DatasourceManagerUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class homeController {
	
	private static final Log log = LogFactory.getLog(homeController.class);
	
	
	@RequestMapping("/")
	public String viewHome(Model model) {
		model.addAttribute("transactions", DatasourceManagerUtil.getTransactions().all());
		return "home";
	}
	
	
	@RequestMapping(value="/getTransactions", method=RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@ResponseBody
	public HashMap<String, Object> getTransactions() {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		return retMap;
	}
	
	
	@RequestMapping(value="/getCompleted", method=RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@ResponseBody
	public HashMap<String, Object> getCompleted() {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		return retMap;
	}
	
	
	@RequestMapping(value="/monitor/getTransactionHistory", method=RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@ResponseBody
	public HashMap<String, Object> getTransactionHistory() {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		return retMap;
	}
	
	
	@RequestMapping(value="/monitor/listFiles", method=RequestMethod.GET)
	@Produces(MediaType.APPLICATION_JSON)
	@ResponseBody
	public HashMap<String, Object> listFiles() {
		HashMap<String, Object> retMap = new HashMap<String, Object>();
		
		return retMap;
	}
	
	
	/*
		recipientId=<destination identifier> Will use the recipientId to send to end point
		file=<multipart file>
		fileFormat=<compliant file format>
		networkServerId=<id of sending network server>
		senderId=<id of sending organization> If not provided then use networkServerId
		fileSize=<float> If not provided then calculate from file parameter
	 */
	@RequestMapping(value="/sendFile", method=RequestMethod.POST)
	public ModelAndView handleFileUpload(
    		@RequestParam(value="recipientId", required=true) Integer recipientId, 
    		@RequestParam(value="file", required=true) MultipartFile file,
    		@RequestParam(value="networkServerId", required=true) Integer networkServerId,
    		@RequestParam(value="senderId") Integer senderId,
    		@RequestParam(value="fileFormat", required=true) String fileFormat,
    		@RequestParam(value="fileSize", defaultValue="0") Long fileSize,
    		RedirectAttributes redir
    	) {
		
		ModelAndView mav = new ModelAndView("redirect:/");
        
		if (!file.isEmpty()) {
            try {
            	
            	/*
            	 * TODO we need to know how to:
            	 *     save a file to a directory (can be within the webapp for demo)
            	 *     write action to database and put the created Transaction into the model
            	 */
            	
                // save file to local directory
            	byte[] bytes = file.getBytes();
                File f = File.createTempFile("edex_", null);
                BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f));
                stream.write(bytes);
                stream.close();
                
                
                // write action to database
                // [RECEIVED FILE] recipientId:p1, neworkServerId:p3, senderId:p4, fileFormat:p5, fileSize:p6
                Transaction tx = new Transaction();
                tx.setRecipientId(recipientId);
                tx.setNetworkServerId(networkServerId);
                tx.setSenderId(senderId==null ? networkServerId : senderId);
                tx.setFileFormat(fileFormat);
                tx.setFileSize(file.getSize());
                tx.setDirection("SEND");
                tx.setSent(new Timestamp(Calendar.getInstance().getTimeInMillis()));
                
                // update response map
                Transaction savedTx = DatasourceManagerUtil.getTransactions().save(tx);
                
                log.debug(String.format(
                	"saved Transaction: {%n  recipientId: %s,%n  networkServerId: %s,%n  senderId: %s,%n  fileFormat: %s%n}",
                	savedTx.getRecipientId(),
                	savedTx.getNetworkServerId(),
                	savedTx.getSenderId(),
                	savedTx.getFileFormat()
                ));
                
                
                redir.addAttribute("error", false);
                redir.addAttribute("status","upload successfull");
                
                // TODO an error will be thrown because this isn't a simple class, need to figure out a workaround
                //redir.addAttribute("transaction", savedTx);
                
                
            } catch (Exception e) {
            	redir.addAttribute("error", true);
            	redir.addAttribute("status", String.format("upload failed: %s", e.getMessage()));
            }
        } else {
        	redir.addAttribute("error", true);
        	redir.addAttribute("status", "missing file");
        }
		
		return mav;
    }
	
	
	// TODO list file:
	// file contents of xaction history DB (will be a file for demo)
}