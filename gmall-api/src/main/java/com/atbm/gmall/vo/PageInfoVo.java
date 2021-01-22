package com.atbm.gmall.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
//有参构造
@AllArgsConstructor
//无参构造
@NoArgsConstructor
@ApiModel
@ToString
@Data
public class PageInfoVo implements Serializable {

    @ApiModelProperty("总记录数")
    private  Long total;

    @ApiModelProperty("总页码")
    private Long totalPage;

    @ApiModelProperty("每页显示记录数")
    private Long pageSize;

    @ApiModelProperty("分页查出的数据")
    private List<? extends Object> list;

    @ApiModelProperty("当前页页码")
    private Long pageNum;

    public static PageInfoVo getVo(IPage iPage,Long size){
        return new PageInfoVo(iPage.getTotal(),iPage.getPages(),size,
                iPage.getRecords(),iPage.getCurrent());
    }

}
