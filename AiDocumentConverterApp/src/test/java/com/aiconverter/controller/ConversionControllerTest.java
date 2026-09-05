package com.aiconverter.controller;

import com.aiconverter.domain.FileFormat;
import com.aiconverter.domain.JobStatus;
import com.aiconverter.entity.ConversionJob;
import com.aiconverter.exception.JobNotFoundException;
import com.aiconverter.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConversionController.class)
class ConversionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

    @Test
    void uploadReturnsAcceptedWithJobId() throws Exception {
        ConversionJob job = new ConversionJob();
        job.setJobId("job-123");
        job.setOriginalFilename("resume.txt");
        job.setFormat(FileFormat.TXT);
        job.setStatus(JobStatus.PENDING);

        when(jobService.submit(any())).thenReturn(job);

        MockMultipartFile file = new MockMultipartFile("file", "resume.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/convert").file(file))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value("job-123"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void statusReturnsJobDetails() throws Exception {
        ConversionJob job = new ConversionJob();
        job.setJobId("job-456");
        job.setOriginalFilename("notes.pdf");
        job.setFormat(FileFormat.PDF);
        job.setStatus(JobStatus.COMPLETED);

        when(jobService.getStatus(eq("job-456"))).thenReturn(job);

        mockMvc.perform(get("/api/convert/job-456/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void statusReturns404ForUnknownJob() throws Exception {
        when(jobService.getStatus(eq("missing"))).thenThrow(new JobNotFoundException("missing"));

        mockMvc.perform(get("/api/convert/missing/status"))
                .andExpect(status().isNotFound());
    }
}
