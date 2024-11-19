package vcix
import chisel3._
import chisel3.util._
import utils._

class TransferReqIO extends Bundle {
    val data1                 = UInt(DLEN.W)   //DLEN
    val data2                 = UInt(DLEN.W)   //DLEN
    val data3_l               = UInt((DLEN).W)   //DLEN
    val funct7                = UInt(7.W)
    val rs2                   = UInt(5.W)
    val rs1                   = UInt(5.W)
    val funct3                = UInt(3.W)
    val rd                    = UInt(5.W)
    val vsew                  = UInt(3.W)
    val vlmul                 = UInt(3.W)
    val vl_in_bytpes_minus_1  = UInt(7.W)
}

class TransferReqExtIO extends Bundle {
    val data3_valid = UInt(1.W)
    val data3_h     = UInt(DLEN.W)   //DLEN
}

class TransferRespIO extends Bundle {
    val resp_bits_data = UInt((2*DLEN).W)   //2*DLEN
}

class TransferIO extends Bundle {
    val req     = Decoupled(new TransferReqIO)
    val req_ext = Output(new TransferReqExtIO)
    val resp    = Flipped(Decoupled(new TransferRespIO))
}

class Transfer extends Module {
    val io = IO(new Bundle{
        val transfer_io = new TransferIO
        val vcix_io     = new VcixIO
    })

    val req_buf_data     = Reg(new TransferReqIO)
    val req_ext          = Reg(new TransferReqExtIO)
    val req_extF         = Reg(new TransferReqExtIO)
    val resp_buf_data    = Reg(new ResponseIO)

    // req_ext.data3_h     := io.transfer_io.req_ext.data3_h & fill(DLEN, io.transfer_io.req_ext.data3_valid).asUInt
    //get data3 high 128bit
    if(io.transfer_io.req_ext.data3_valid) {
        req_ext.data3_h   := io.transfer_io.req_ext.data3_h
    }
    
    //fot timing to add flop(buffer) 
    if(io.vcix_io.req.ready && io.transfer_io.req.valid) {
        req_buf_data :=  io.transfer_io.req
        req_extF     :=  req_ext
    }

    //consider only one data ready
    if(io.transfer_io.req.valid)
        io.vcix_io.req.valid   := true.B
    else if(io.vcix_io.req.ready)
        io.vcix_io.req.valid   := false.B
       
    //when buffer is empty, save data to buffer to reduce latency
    io.transfer_io.req.ready := io.vcix_io.req.ready || ~io.vcix_io.req.valid

    io.vcix_io.req          <> req_buf_data
    io.vcix_io.req.data3    := Cat(req_extF.data3_h, req_buf_data.data3_l)


    if(io.vcix_io.resp.valid){
        io.transfer_io.resp.valid   := true.B
    }
    else if(io.transfer_io.resp.ready){
        io.transfer_io.resp.valid   := false.B
    }

    if(io.vcix_io.resp.ready && io.vcix_io.resp.valid){
        resp_buf_data  := io.vcix_io.resp
    }

    io.vcix_io.resp.ready := io.transfer_io.resp.ready && ~io.transfer_io.resp.valid


    // val req_ext   = Reg(new TransferReqExtIO)
    // val vcix_flop = Reg(new RequestIO)
    // val resp_flop = Reg(new TransferRespIO)

    // val req_ext = Output(new TransferRespIO)
    // // req_ext.data3_valid := io.transfer_io.req_ext.data3_valid
    // req_ext.data3_h     := io.transfer_io.req_ext.data3_h & fill(DLEN, io.transfer_io.req_ext.data3_valid).asUInt

    // when(io.transfer_io.req.valid & io.transfer_io.req.ready){
    //     vcix_flop.data3     := Cat(req_ext.data3_h, io.transfer_io.req.data3_l)
    //     vcix_flop           <> io.transfer_io.req
    // }

    // when(io.transfer_io.resp.valid & io.transfer_io.resp.ready) {
    //     resp_flop <> io.transfer_io.resp_flop
    // }
}

