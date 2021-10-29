package personal.springutility.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import personal.springutility.dto.PartOfPageDto;
import personal.springutility.dto.UserAddPageDto;
import personal.springutility.exception.DataNotFound;
import personal.springutility.exception.ServerError;
import personal.springutility.model.journal.Page;
import personal.springutility.model.journal.RatingScale;
import personal.springutility.model.journal.UserCreatedPage;
import personal.springutility.repository.PageRepository;
import personal.springutility.repository.UserCreatedPageRepository;
import personal.springutility.util.Mappers;
import personal.springutility.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Service
public class JournalService {

    private static final String ADD_ERROR = "Could not add new page";
    private static final String ADD_ERROR_USER = ", invalid user id : %d ";
    private static final String FIND_ERROR ="Could not retrieve data";

    private final PageRepository pageRepository;
    private final UserCreatedPageRepository userCreatedPageRepository;
    private final StringUtils stringUtils;


    public JournalService(PageRepository pageRepository, UserCreatedPageRepository userCreatedPageRepository, StringUtils stringUtils) {
        this.pageRepository = pageRepository;
        this.userCreatedPageRepository = userCreatedPageRepository;
        this.stringUtils = stringUtils;

    }


    public void addPage(Integer userId, UserAddPageDto userAddPageDto) {
        try {
            UserCreatedPage userCreatedPage = userCreatedPageRepository.findByUserId(userId);
            Page page = toPage(userAddPageDto);
            page.setUserCreatedPage(userCreatedPage);
            pageRepository.save(page);
        } catch (DataAccessException ex) {
            throw new ServerError(ADD_ERROR);
        } catch (NullPointerException ex) {
            throw new DataNotFound(String.format(ADD_ERROR + ADD_ERROR_USER, userId));
        }
    }

    public List<PartOfPageDto> findAll(Integer userId, Integer createdPageId) {
        try {
            List<Page> pages = pageRepository.findAll(userId,createdPageId);
            return toPartOfPageDtoList(pages);
        } catch (DataAccessException ex) {
            throw new DataNotFound(FIND_ERROR);
        }catch (NullPointerException ex) {
            throw new DataNotFound("Failed retrieving data");
        }
    }
    private Page toPage(UserAddPageDto userAddPageDto) {
        return Page.builder()
                .title(userAddPageDto.getTitle())
                .content(userAddPageDto.getContent())
                .created(userAddPageDto.getCreated())
                .emoji(stringUtils.toByte(userAddPageDto.getEmoji()))
                .scale(RatingScale.of(userAddPageDto.getScale()))
                .build();
    }

    private List<PartOfPageDto> toPartOfPageDtoList(List<Page> pages){
        return pages
                .stream()
                .map(page ->
                     PartOfPageDto.builder()
                            .id(page.getId())
                            .created(page.getCreated())
                            .title(page.getTitle())
                            .emoji(stringUtils.toBase64(page.getEmoji()))
                            .build())
                .collect(Collectors.toList());
    }
}