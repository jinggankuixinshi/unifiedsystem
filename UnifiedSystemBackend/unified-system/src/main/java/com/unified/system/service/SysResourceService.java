package com.unified.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.unified.system.dto.SortItemDTO;
import com.unified.system.dto.SysResourceDTO;
import com.unified.system.entity.SysResource;
import com.unified.system.vo.SysResourceVO;

import java.util.List;

public interface SysResourceService extends IService<SysResource> {

    List<SysResourceVO> getResourceTree();

    SysResource createResource(SysResourceDTO dto);

    SysResource updateResource(Long id, SysResourceDTO dto);

    boolean deleteResource(Long id);

    void sortResources(List<SortItemDTO> items);
}
